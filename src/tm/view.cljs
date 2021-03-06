(ns tm.view
  (:require [tm.world :as world]
            [clojure.string :as string]
            [reagent.core :as reagent]))
(def tm-box-color "#c68c53")
(def tm-state-colors {"running"     "dimgray"
                      "halt_acc"    "lawngreen"
                      "halt_no_acc" "red"})
(def table-hl-color "#ff6f00")

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

(defn sim-step! []
  (swap! world/app-state world/tm-step))

(defonce tm-speed
  (reagent/atom 0))

(defonce sim-clock
  (reagent/atom 0))

(defn tick! []
  (+ 1 @sim-clock)
  (when (and (not= @tm-speed 0) (= (mod @sim-clock @tm-speed) 0))
    (sim-step!)))

(defonce input-state
  (reagent/atom (string/join (:tape @world/app-state))))

(defn change-input [input c]
  (if (and (not= c \ )
      (some #{c} (:alphabet @world/app-state)))
    (str input c)
    input))

(defn handle-keydown! [e]
  (when-let [c (.fromCharCode js/String (.-keyCode e))]
  (swap! input-state change-input c)))

; Helper function to compute starting character for displaying
; a string.
; TODO: Assumes a certain font size
(defn start-input-char [box-width nchars]
  (max 0 (- nchars (/ box-width 13))))

; Helper function to truncate string to fix in box
; TODO: Assumes a certain font size
(defn truncate-for-box [s box-width]
  (subs s 0 (/ box-width 9)))

(defn move-str [s a moves fstate]
  (if-let [m (first (filter #(let [[s2 a2 _ _ _] %]
                    (and (= s s2) (= a a2))) moves))]
    (str (get m 2) (if (= \ (get m 3)) "-" (get m 3))
                   (if (= 1 (get m 4)) "R" "L"))
    (if (contains? fstate s) "ACC" "REJ")))

(defn tm-view [{:as world :keys [rstate fstate cstate size labels alphabet ccell moves tape]}]
  [:div {:style {:font-family "Courier New"
                 :text-align "center"}}
    [:table {:style {:width "300px"}}
      [:thead [:tr [:td ]
        (for [a (identity alphabet)] [:th (if (= \  a) "-" (identity a))])]]
      [:tbody
        ; (for [s (range size)] [:tr [:td [:b (identity s)]]
        (for [s (range size)] [:tr [:td [:b
          (if (= s cstate) [:span {:style {:color table-hl-color}} (identity s)]
                           (identity s))]]
          (for [a (identity alphabet)] [:td
            (if (and (= s cstate) (= a (nth tape ccell)))
              [:b [:span {:style {:color table-hl-color}} (move-str s a moves fstate)]]
              (move-str s a moves fstate))])])]]
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
      [:rect {:x 180
              :y 110
              :width 405
              :height 20
              :fill "black"
              :stroke-width 0
              :stroke "black"}]
      ; Current state
      [:text {:x 184
              :y 125
              :fill "aqua"
              :font-family "monospace"
              :font-size "15px"} (truncate-for-box (get labels cstate) 400)]
      ; Go button
      [:circle {:cx 685
                :cy 116 
                :r  5
                :stroke "black"
                :stroke-width 1
                :fill (if (> @tm-speed 0) "lawngreen" "dimgray")
                :onClick #(swap! tm-speed (fn [] 1))}]
      ; Stop button
      [:circle {:cx 685
                :cy 139
                :r  5
                :stroke "black"
                :stroke-width 1
                :fill (if (> @tm-speed 0) "dimgray" "red")
                :onClick #(swap! tm-speed (fn [] 0))}]
      ; Step button
      [:circle {:cx 685
                :cy 162
                :r  5
                :stroke "black"
                :stroke-width 1
                :fill "dimgray"
                :onClick #(sim-step!)}]
      ; Tape input box
      [:rect {:x 475
              :y 175
              :width  197
              :height  18
              :fill "black"
              :stroke-width 2
              :stroke "black"}]
      ; Tape input
      [:text {:x 477
              :y 190
              :fill "aqua"
              :font-family "monospace"
              :font-size "20px"}
                ; TODO: Get rectangle width from input box
                (subs @input-state (start-input-char 197
                                     (count @input-state)))]
      ; Clear button
      [:circle {:cx 462
                :cy 185
                :r  5
                :stroke "black"
                :stroke-width 1
                :fill "white"
                :onClick #(swap! input-state (fn [] (str)))}]
      ; Reset button
      [:circle {:cx 685
                :cy 185
                :r  5
                :stroke "black"
                :stroke-width 1
                :fill "dimgray"
                :onClick #(world/restart! @input-state)}]
      ; Labels
      [:text {:x 150
              :y 185
              :fill "black"
              :font-size "50px"} "TM 3000"]
      [:text {:x 110
              :y 125
              :fill "black"
              :font-size "20px"} "State"]
      [:text {:x 647
              :y 120
              :fill "green"
              :font-size "20px"} "Go"]
      [:text {:x 625
              :y 143
              :fill "red"
              :font-size "20px"} "Stop"]
      [:text {:x 625
              :y 166
              :fill "yellow"
              :font-size "20px"} "Step"]
      [:text {:x 475
              :y 166
              :fill "black"
              :font-size "20px"} "Reset"]]])

(defn root-view []
  [tm-view @world/app-state])
