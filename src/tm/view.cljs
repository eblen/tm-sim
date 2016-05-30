(ns tm.view
  (:require [tm.world :as world]
            [clojure.string :as string]))

(def tm-box-color "#c68c53")
(def tm-state-colors {"running"     "dimgray"
                      "halt_acc"    "lawngreen"
                      "halt_no_acc" "red"})
(defn tape-cell [x]
  [:rect {:x x
          :y 50
          :width 30
          :height 50
          :stroke "black"
          :stroke-width 5
          :fill "white"}])

(defn tape-symbol [x c]
  [:text {:x (+ x 3)
          :y 90
          :fill "black"
          :font-size "40px"} c])

(defn tm-view [{:as world :keys [rstate cstate ccell tape]}]
  [:div {:style {:font-family "Courier New"
                 :text-align "center"}}
    [:svg {:width 1000 :height 500}
      ; TM state light
      [:circle {:cx 415
                :cy 30
                :r  15
                :stroke (get tm-state-colors rstate)
                :stroke-width 5
                :fill (get tm-state-colors rstate)}]
      ; Tape
      (for [i (range (max 0 (- 10 ccell)) 20)]
        [tape-cell (+ 100 (* i 30))])
      (for [i (range 0 20)
            :let [cell (- i (- 10 ccell)) ncells (count tape)]
            :when (and (> cell -1) (< cell ncells))]
        [tape-symbol (+ 100 (* i 30)) (nth tape cell)])
      ; Current tape cell slot
      [:rect {:x 400
              :y 30
              :width 30
              :height 20
              :fill tm-box-color
              :stroke-width 5
              :stroke tm-box-color}]
      [:rect {:x 400
              :y 50
              :width 30
              :height 50
              :fill-opacity 0.0
              :stroke-width 5
              :stroke tm-box-color}]
      ; TM Box
      [:rect {:x 100
              :y 100
              :width 600
              :height 100
              :fill tm-box-color
              :stroke-width 5
              :stroke "black"}]
      ; Current state indicator
      [:rect {:x 385
              :y 120
              :width 60
              :height 60
              :fill "white"
              :stroke-width 5
              :stroke "black"}]
      ; Current state
      [:text {:x 392
              :y 160
              :fill "black"
              :font-size "40px"} cstate]]])

(defn root-view []
  [tm-view @world/app-state])
