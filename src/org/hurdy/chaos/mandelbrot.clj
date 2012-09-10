(ns org.hurdy.chaos.mandelbrot
  (:use [org.hurdy.chaos.complex]))

(defn calc-path
  [x y max-iterations]
  (let  [c [x y]]
    (loop [z c
           path []
           iterations 0]
      (if (> iterations max-iterations)
        []
        (if (> (abs z) 2.0)
          (conj path z)
          (recur (add c (multiply z z)) (conj path z) (inc iterations)))))))