(defproject org.hurdy.chaos "1.0.0"
  :description "Clojure Chaos"
  :dependencies [
                  [org.clojure/clojure "1.4.0"]
                  [org.lwjgl.lwjgl/lwjgl "2.8.4"]
                ]
  :jvm-opts ["-Djava.library.path=./resources/native/macosx:./resources/native/windows:./resources/native/linux:./resources/native/solaris"]
  :plugins  [
              [lein-marginalia "0.7.0"]
            ]
  :main org.hurdy.chaos.core
)
