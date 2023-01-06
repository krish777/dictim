(ns dictim.core-test
  (:require [clojure.test :refer :all]))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 0 1))))

(def ex "direction:right
         ctr1:CTR1 {
           style.fill:blue
           a->b: Hi Jude{label:true}
         }
         ctr2: CTR2{
           a->b: Hi Jude{label:true}
           simon: SL {style: {fill: blue}; opacity:1.0}
           a-> simon: random
         }
         outside: Outside the circle {
           bridget: Bridget
         }")

(def ex-bad "direction:right
         ctr1:CTR1 {style.fill:blue;a->b: Hi Jude{label:true}}
         ctr2: CTR2{a->b: Hi Jude{label:true}
           simon: SL {style: {fill: blue}; opacity:1.0}
           a-> simon: random}
         outside: Outside the circle {
          bridget: Bridget
         }")


(def ex-graph
  [:jude "Friends"
   [:tristram "T Biggs expanded"
    [:tris "TAB"]
    [:maddie "Madeline"]
    [:tris "--" :maddie "wedding bells?"]
    [:tris "->" :children "previously sired"]
    [:children "the brood"
     ["Oliver" "Eldest" {:style {:fill "orange"}}]
     ["Roof" "Good footballer" {:shape "person"}]]]])


(def ex-sequence
  [:convs "Office Conversations"
   [:conv1 "Office conversation 1"
    {:shape "sequence_diagram" :order '(:bob :alice)}
    [:alice "Alice" {:shape "person" :style {:fill "orange"}}]
    [:bob "Bobby"]
    ["awkward small talk"
     [:alice "->" :bob "um, hi"]
     [:bob "->" :alice "oh, hello"]
     ["icebreaker attempt"
      [:alice "->" :bob "what did you have for lunch?"]]
     [:fail {:style {:fill "green"}}
      [:bob "->" :alice "that's personal"]]]]
   [:conv2 "Office conversation 2"
    {:shape "sequence_diagram" :order '(:simon :trev)}
    [:simon "Simon" {:shape "person"}]
    [:trev "Trevor"]
    ["failed conversation"
     [:simon "->" :trev "seen the football"]
     [:trev"->" :simon "no, I was at my gran's"]
     ["Carry on anyway"
      [:simon "->" :trev "mate, you missed a classic"]]]]
   [:conv1 "->" :conv2 "spot the difference?"]])


(def ex-class
  [:MyClass { :shape "class" :field "\"[]string\"" :-reader "io.RuneReader"}])


(def ex1
  [{:direction "right"}
   [:mike "Old friends"
    [:comment "a diagram of friends"]
    [:t1 "T Boggs expanded"
     [:tris "TAB"]
     [:mads "Madeline"]
     [:tris "--" :maddie "wedding bells?"]]]])


(def dis [{:type :attrs, :key 1772842048, :meta {:a 1}, :posn [0]}
          {:type :ctr, :key :jude, :meta {:label "Friends"}, :posn [1]}
          {:type :cmt, :key "a diagram of friends", :meta nil, :posn [1 0]}
          {:type :ctr,
           :key :tristram,
           :meta {:label "T Biggs expanded"},
           :posn [1 1]}
          {:type :shape, :key :tris, :meta {:label "TAB"}, :posn [1 1 0]}
          {:type :conn,
           :key [:tris "--" :maddie],
           :meta {:label "wedding bells?"},
           :posn [1 1 1]}
          {:type :conn,
           :key [:tris "->" :children],
           :meta {:label "previously sired"},
           :posn [1 1 2]}
          {:type :ctr,
           :key :children,
           :meta {:label "the brood"},
           :posn [1 1 3]}
          {:type :shape,
           :key "Oliver",
           :meta {:label "Eldest", :attrs {:style {:fill "orange"}}},
           :posn [1 1 3 0]}
          {:type :shape,
           :key "Roof",
           :meta {:label "Good footballer", :attrs {:shape "person"}},
           :posn [1 1 3 1]}
          {:type :attrs, :key -508115573, :meta {:b 2}, :posn [2]}])
