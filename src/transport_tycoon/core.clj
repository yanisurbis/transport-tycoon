(ns transport-tycoon.core
  (:require [clojure.pprint :refer :all])
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

(def queue-to-deliver [:a :b :b])

(def system {:initial-queue queue-to-deliver
             :current-time 0
             :queues {:factory queue-to-deliver
                      :port []
                      :a []
                      :b []}
             :events []
             :history []
             :actors [car-1 car-2 ship-1]})

(defn is-completed-event? [event system]
  (>= (:current-time system)
    (+ (:start-time event) (get-in event [:action :duration]))))

(defn get-completed-events [system]
  (->>
    (:events system)
    (filter #(is-completed-event? % system))
    (map #(assoc % :end-time (:current-time system)))))

(defn delivery-in-process? [system]
    (not=
      (count (:initial-queue system))
      (+ (count (get-in system [:queues :a]))
        (count (get-in system [:queues :b])))))

(defn get-element-from-queue [system queue-name]
  (-> (get-in system [:queues queue-name])
      first))

(defn remove-element-from-queue [system queue-name]
  (update-in system [:queues queue-name] rest))

(defn get-random-id []
  (rand-int 10000000))

(defn prn! [data text]
  (println)
  (pprint (str "START~" text))
  (pprint data)
  (println (str "END~" text))
  (println))

(defn get-next-car-action-type [system car]
  (let [payload (get-element-from-queue system :factory)
        actor-position (:position car)]
        ;(prn! [(:queues system) payload actor-position] "action-type")
       (case [payload actor-position]
          [:a :factory] :factory->port
          [:b :factory] :factory->b
          [:a :port] :port->factory
          [:b :port] :port->factory
          [nil :port] :port->factory
          [:a :b] :b->factory
          [:b :b] :b->factory
          [nil :b] :b->factory
          :wait)))

(defn get-next-ship-action-type [system ship]
  (let [payload (get-element-from-queue system :port)
        actor-position (:position ship)]
       (case [payload actor-position]
          [:a :a] :a->port
          [nil :a] :a->port
          [:a :port] :port->a
          :wait)))

(defn get-next-action-type [system actor]
  (case (:type actor)
    :car (get-next-car-action-type system actor)
    :ship (get-next-ship-action-type system actor)))

(defn get-next-action [system actor]
  (get-in actor [:actions
                (get-next-action-type system actor)]))

(defn get-payload-and-new-system [system actor]
 (case [(:type actor) (:position actor)]
    [:car :factory] {:payload (get-element-from-queue system :factory)
                     :system (remove-element-from-queue system :factory)}
    [:ship :port] {:payload (get-element-from-queue system :port)
                   :system (remove-element-from-queue system :port)}
                  {:payload nil
                  :system system}))

(defn add-event-for-actor [system actor]
  (let [action (get-next-action system actor)
        {:keys [system payload]} (get-payload-and-new-system system actor)
        event {:id (get-random-id)
               :actor actor
               :action action
               :start-time (:current-time system)
               :payload payload
               :end-time nil }]
    (update system :events conj event)))

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
  ;{:pre [(do (prn [actor event] "update-actor-start") true)]
  ; :post [(do (prn % "update-actor-end") true)]}
  (if (= (:id actor) (get-in event [:actor :id]))
    (update-position actor (get-in event [:action :to]))
    actor))

(defn update-actors [system event]
  (assoc system :actors (map #(update-actor % event) (:actors system))))

(defn process-completed-events [system]
  (reduce
    (fn [system event]
      (-> system
          (update-queues event)
          (update-actors event)))
    system
    (get-completed-events system)))

(defn generate-new-events [system]
  (reduce (fn [system actor]
            ;(prn actor "actor")
            (add-event-for-actor system actor))
           system
           (:actors system)))

(defn remove-completed-events [system]
  (merge system
    {:events (remove #(is-completed-event? % system) (:events system))
     :history (into (:history system) (get-completed-events system))}))

(defn update-time [system]
  (update system :current-time inc))

(defn tick [system]
  (->> system
    process-completed-events
    generate-new-events
    remove-completed-events
    update-time))

;(remove-completed-events system)
;
;(->> system
;  process-completed-events
;  generate-new-events
;  ;remove-completed-events
;  update-time
;  process-completed-events
;  ;generate-new-events
;  ;remove-completed-events
;  update-time)

(defn print-event [event]
  (pprint [(:actor-id event) (:start-time event) (get-in event [:action :type]) (:payload event)])
  ;(pprint event)
  ;(println "-----------------------------------")
  (println)
  event)

(defn pprint-system! [system]
  (->>
    (into (:events system) (:history system))
    (remove #(= :wait (get-in % [:action :type])))
    (map #(assoc % :actor-id (get-in % [:actor :id])))
    (map #(assoc % :action-type (get-in % [:action :type])))
    (map #(dissoc % :actor))
    (sort-by :start-time)
    (sort-by :actor-id)
    (map #(select-keys % [:actor-id :start-time :end-time :action-type :payload]))
    (reduce conj [])
    print-table)
  (println (:current-time system))
  (println "-----------------------------"))

(->> system
  (iterate tick)
  (drop-while delivery-in-process?)
  (take 1)
  (map pprint-system!)
  doall)