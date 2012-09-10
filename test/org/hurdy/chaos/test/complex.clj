(ns org.hurdy.chaos.test.complex
  (:use [org.hurdy.chaos.complex])
  (:use [clojure.test]))

(deftest test-add []
  (is
      (= (add {:r 1 :i 2} {:r 3 :i -1}) {:r 4 :i 1})
  )
)

(deftest test-multiply []
  (is
      (= (multiply {:r 5 :i 2} {:r 4 :i 3}) {:r 14 :i 23})
  )
)