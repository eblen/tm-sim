(ns ^:figwheel-always tm.core
    (:require [tm.world :as world]
              [tm.view :as view]
              [reagent.core :as reagent]))

(enable-console-print!)

(defn on-js-reload []
  (println "Reloaded...")
  (reagent/render-component [view/root-view] (. js/document (getElementById "app"))))

(defn init []
  (on-js-reload)
  (js/setInterval world/tick! 1000))

(defonce start
  (init))
