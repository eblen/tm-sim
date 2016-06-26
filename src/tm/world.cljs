(ns tm.world
  (:require [reagent.core :as reagent]
            [tm.machines  :as machines]))

(defn find-num-states [moves]
  (+ 1 (reduce #(max %1 (get %2 0) (get %2 2)) 0 moves)))

(defn create-tm [{:keys [input sstate fstate labels moves]}]
  {:sstate sstate
   :fstate fstate
   :cstate sstate
   :size   (find-num-states moves)
   :labels labels
   :ccell  0
   :alphabet [\0,\1,\ ]
   :moves  moves
   :tape   (vec input)
   :rstate "running"})

(defn- nmove [tm]
  (let [cstate (tm :cstate)
        csymbol (get (tm :tape) (tm :ccell))]
    (first (filter #(and (= (get % 0) cstate) (= (get % 1) csymbol)) (tm :moves)))))

(defn- append-tape [tape ncell]
  (if (= ncell (count tape))
    (conj tape " ")
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
       (assoc tm :cstate nstate :ccell (max 0 ncell) :tape ntape :rstate nrstate))
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
  (assoc (reset-tm tm) :tape (append-tape (vec input) 0)))

(defonce app-state
  (reagent/atom (create-tm machines/tm1)))

(defn restart! [input]
  (swap! app-state set-tape input))
