(ns transport-tycoon.core
  (:gen-class))

(def car-1 {:id :car-1
            :type :car
            :actions {:factory->b {:type :factory->b
                                   :duration 5
                                   :to :b}
                      :b->factory {:type :b->factory
                                   :duration 5
                                   :to :factory}
                      :factory->port {:type :factory->port
                                      :duration 1
                                      :to :port}
                      :port->factory {:type :port->factory
                                      :duration 1
                                      :to :factory}
                      :wait {:type :wait
                             :duration 1}}
            :position :factory})

(def car-2 (assoc car-1 :id :car-2))

(def ship-1 {:id :ship-1
             :type :ship
             :actions {:port->a {:type :port->a
                                 :duration 4
                                 :to :a}
                       :a->port {:type :a->port
                                 :duration 4
                                 :to :port}
                       :wait {:type :wait
                              :duration 1}}
             :position :port})

(def event-example {:id 123
                    :actor car-1
                    :payload :a
                    :action {:type :factory->b
                             :duration 5}
                    :start-time 10})

(defn is-completed-event? [event current-time]
  (= current-time
    (+ (:start-time event) (get-in event [:action :duration]))))

(defn get-completed-events [system]
  (filter #(is-completed-event? % (:current-time system))
    (:running system)))

(defn delivery-in-process? [queue-to-deliver]
  (fn [system]
    (not=
      (count queue-to-deliver)
      (+ (count (get-in system [:queues :a]))
        (count (get-in system [:queues :b]))))))

(defn get-element-from-queue [system queue-name]
  (-> (get-in system [:queues queue-name])
      first))

(defn get-random-id (rand-int 100000000000000))

(defn get-next-car-action-type [system car]
  (let [payload (get-element-from-queue system :factory)
        actor-position (:position car)
        (case [payload actor-position]
          [:a :factory] :factory->port
          [:b :factory] :factory->b
          [:a :port] :port->factory
          [:b :port] :port->factory
          [nil :port] :port->factory
          [:a :b] :b->factory
          [:b :b] :b->factory
          [nil :b] :b->factory
          :wait)]))

(defn get-next-ship-action-type [system ship]
  (let [payload (get-element-from-queue system :port)
        actor-position (:position ship)
        (case [payload actor-position]
          [:a :a] :a->port
          [nil :a] :a->port
          [:a :port] :port->a
          :wait)]))

(defn get-next-action-type [system actor]
  (case (:type actor)
    :car (get-next-car-action-type system actor)
    :ship (get-next-ship-action-type system actor)))

(defn get-payload [system actor]
 (case [(:type actor) (:position actor)]
    [:car :factory] (get-element-from-queue system :factory)
    [:ship :port] (get-element-from-queue system :port)
    nil))

(defn get-next-event [system actor]
  {:id (get-random-id)
   :actor actor
   :action (get-in actor [:actions
                          (get-next-action-type system actor)])
   :start-time (:current-time system)
   :payload (get-payload system actor)})

(defn add-payload-to-queue [system queue-name payload]
 (update-in system [:queues queue-name] conj payload))

(defn update-queues [system event]
  (let [payload (:payload event)
        action-type (get-in event [:action :type])]
       (case action-type
        :factory->port (add-payload-to-queue system :port payload)
        :factory->b (add-payload-to-queue system :b payload)
        :port->a (add-payload-to-queue system :a payload)
        system)))

(defn update-position [actor new-position]
  (if new-position
    (assoc actor :position new-position)
    actor))

(defn update-actor [actor event]
  (if (= (:id actor) (get-in actor [:actor :id]))
    (update-position actor (get-in event [:action :to]))))

(defn update-actors [system event]
  (assoc system :actors (map #(update-actor % event) (:actors system))))

(defn process-completed-events [system]
  (reduce
    (fn [system event]
      (-> system
        (update-actors event)
        (update-queues event)))
    system
    (get-completed-events system)))

(defn generate-new-events [system]
  (update system :events
    merge (map #(get-next-event system %) (:actors system))))

(defn remove-completed-events [system]
  (merge system
    {:events (remove is-completed-event? (:events system))
     :history (merge (get-completed-events system) (:history system))}))

(defn update-time [system]
  (update system :current-time inc))

(defn tick [system]
  (-> system
    process-completed-events
    generate-new-events
    remove-completed-events
    update-time))

(def queue-to-deliver [:a])

(def system {:initial-queue queue-to-deliver
             :current-time 0
             :queues {:factory queue-to-deliver
                      :port []
                      :a []
                      :b []}
             :events []
             :history []
             :actors '(car-1 car-2 ship-1)})

(->> system
  (iterate tick)
  (drop-while (delivery-in-process? queue-to-deliver))
  (take 1)
  :current-time)
