(ns transport-tycoon.core-test
  (:require [clojure.test :refer :all]
            [transport-tycoon.core :as core]))

(deftest basics
  (testing "is-completed-event"
    (is (= (core/is-completed-event? {:start-time 0 :action {:duration 1}}
                                     {:current-time 1})
           true))))
