(ns tm.machines)

; Evil TM. Reject string with a 0 by running forever.
(def tm1 {:input "1111"
          :sstate 0
          :fstate #{2}
          :labels ["Okay - no 0s found so far"
                   "Evil 0 found - endless march right has begun"
                   "Accepted - string is pure"]
          :moves #{[0 \0 1 \0  1]
                   [0 \1 0 \1  1]
                   [0 \  2 \   1]
                   [1 \0 1 \0  1]
                   [1 \1 1 \1  1]
                   [1 \  1 \   1]}})

; Weird TM. Reject string with a 1 by going left until tape exhausted.
(def tm2 {:input "0000"
          :sstate 0
          :fstate #{2}
          :labels ["Okay - no 1s found so far"
                   "Evil 1 found - will be rejected"
                   "Accepted - string is pure"]
          :moves #{[0 \0 0 \0  1]
                   [0 \1 1 \1 -1]
                   [0 \  2 \   1]
                   [1 \0 1 \0 -1]
                   [1 \1 1 \1 -1]
                   [1 \  1 \  -1]}})
