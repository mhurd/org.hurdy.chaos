(ns org.hurdy.chaos.mandelbrot
  (:use [org.hurdy.chaos.complex]))

(defn calc-path
  [x y max-iterations]
  (let  [c (hash-map :r x :i y)]
    (loop [z c
           iterations 1]
      (if (>= iterations max-iterations)
        (if (> (abs z) 2.0)
          {:in-set false :point c :iterations iterations}
          {:in-set true :point c :iterations iterations}
        )
        (if (> (abs z) 2.0)
          {:in-set false :point c :iterations iterations}
          (recur (add c (multiply z z)) (inc iterations))
        )
      )
    )
  )
)