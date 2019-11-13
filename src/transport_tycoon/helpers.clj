(ns transport-tycoon.helpers
  (:require [clojure.pprint :refer :all]))

(defn prn! [data text]
  (println)
  (pprint (str "START~" text))
  (pprint data)
  (println (str "END~" text))
  (println))

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
  (println (- (:current-time system) 1))
  (println "-----------------------------"))
