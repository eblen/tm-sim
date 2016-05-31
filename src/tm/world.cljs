(ns tm.world
  (:require [reagent.core :as reagent]))

(defn tm [input sstate fstate moves]
  {:sstate sstate
   :fstate fstate
   :cstate sstate
   :ccell  0
   :alphabet #{\0,\1}
   :moves  moves
   :tape   (vec input)
   :rstate "running"})

(defn- nmove [tm]
  (let [cstate (tm :cstate)
        csymbol (get (tm :tape) (tm :ccell))]
    (first (filter #(and (= (get % 0) cstate) (= (get % 1) csymbol)) (tm :moves)))))

(defn- append-tape [tape ncell]
  (if (= ncell (count tape))
    (conj tape "_")
    tape))

(defn- tm-step-impl [tm]
  (cond
   (not= (tm :rstate) "running") tm
   :else
   (if-let [[_ _ nstate nsymbol ndir] (nmove tm)]
     (let [ncell (+ (tm :ccell) ndir)
           ntape (assoc (append-tape (tm :tape) ncell) (tm :ccell) nsymbol)
           nrstate
           (cond (< ncell 0) "halt_no_acc"
                 (contains? (tm :fstate) nstate) "halt_acc"
                 :else "running")]
       (assoc tm :cstate nstate :ccell ncell :tape ntape :rstate nrstate))
     (assoc tm :rstate "halt_no_acc"))))

(defn tm-multistep [tm n]
  (if (> n 0)
    (recur (tm-step-impl tm) (dec n))
    tm))

(defn tm-step [tm]
  (tm-multistep tm 1))

(defn reset-tm [tm]
  (assoc tm :cstate (tm :sstate) :ccell 0 :rstate "running"))

(defn set-tape [tm input]
  (assoc (reset-tm tm) :tape (vec input)))

; Evil TM - reject any string with a 0 by running forever.
(def q1 "Okay - no 0s found so far")
(def q2 "Evil 0 found - endless march right has begun")
(def q3 "Accepted - string is pure")
(def atm (tm "1111" q1 #{q3}
             #{[q1 \0 q2 \0 1]
               [q1 \1 q1 \1 1]
               [q1 "_" q3 "_" 1]
               [q2 \0 q2 \0 1]
               [q2 \1 q2 \1 1]
               [q2 "_" q2 "_" 1]}))

; Weird TM - reject any string with a 1 by going left until the tape is
; exhausted.
(def q4 "Okay - no 1s found so far")
(def q5 "Evil 1 found - will be rejected")
(def q6 "Accepted - string is pure")
(def atm2 (tm "0000" q4 #{q6}
             #{[q4 \0 q4 \0 1]
               [q4 \1 q5 \1 -1]
               [q4 "_" q6 "_" 1]
               [q5 \0 q5 \0 -1]
               [q5 \1 q5 \1 -1]
               [q5 "_" q5 "_" -1]}))

(defonce app-state
  (reagent/atom atm2))

(defn restart! [input]
  (swap! app-state set-tape input))
