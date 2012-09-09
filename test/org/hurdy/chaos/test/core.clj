(ns org.hurdy.chaos.test.core
  (:use [org.hurdy.chaos.core])
  (:use [clojure.test]))

(deftest test-bar
  (is
    (=
      (bar)
      1))
  )