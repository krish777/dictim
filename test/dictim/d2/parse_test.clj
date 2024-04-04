(ns dictim.d2.parse-test
  (:require [clojure.test :refer :all]
            [dictim.d2.parse :as p]
            [dictim.validate :as v]
            [instaparse.core :as insta]
            [dictim.d2.compile :as c]))

;; parsing

;; This ns is Clojure only to allow for all test d2 to be placed in
;; files in the d2 sub-directory.

(def num-parses #'p/num-parses)


(deftest shapes
  (testing "Shapes"
    (let [d2 (slurp "test/dictim/d2/samples/shapes.d2")
          dict (p/dictim d2)]
      (is (= 1 (num-parses d2)))
      (is (= true (v/all-valid? dict :d2)))
      (is (= dict
             '(["imAShape"]
               ["im_a_shape"]
               ["im a shape"]
               ["i'm a shape"]
               [:comment "notice that one-hyphen is not a connection"]
               [:comment "whereas, `a--shape` would be a connection"]
               ["a-shape"]
               [:list ["SQLite"] ["Cassandra"]]
               ["pg" "PostgreSQL"]
               ["Cloud" "my cloud"]
               {"Cloud.shape" "cloud"}))))))


(deftest containers
  (testing "Containers"
    (let [d2 (slurp "test/dictim/d2/samples/containers.d2")
          dict (p/dictim d2)]
      (is (= 1 (num-parses d2)))
      (is (= true (v/all-valid? dict :d2)))
      (is (= dict
             '(["server"]
               [:comment "Declares a shape inside of another shape"]
               ["server.process"]
               [:comment "Can declare the container and child in same line"]
               ["im a parent.im a child"]
               [:comment "Since connections can also declare keys, this works too"]
               ["apartment.Bedroom.Bathroom"
                "->"
                "office.Spare Room.Bathroom"
                "Portal"]
               ["clouds"
                ["aws" ["load_balancer" "->" "api"] ["api" "->" "db"]]
                ["gcloud" ["auth" "->" "db"]]
                ["gcloud" "->" "aws"]]
               ["clouds"
                ["aws" "AWS" ["load_balancer" "->" "api"] ["api" "->" "db"]]
                ["gcloud" "Google Cloud" ["auth" "->" "db"]]
                ["gcloud" "->" "aws"]]
               ["users" "->" "clouds.aws.load_balancer"]
               ["users" "->" "clouds.gcloud.auth"]
               ["ci.deploys" "->" "clouds"]
               ["christmas" ["presents"]]
               ["birthdays"
                ["presents"]
                ["_.christmas.presents" "->" "presents" "regift"]
                {"_.christmas.style.fill" "\"#ACE1AF\""}]))))))


(deftest connections
  (testing "Connections"
    (let [d2 (slurp "test/dictim/d2/samples/connections.d2")
          dict (p/dictim d2)]
      (is (= 1 (num-parses d2)))
      (is (= true (v/all-valid? dict :d2)))
      (is (= dict
             '(["Write Replica Canada" "<->" "Write Replica Australia"]
               ["Read Replica" "<-" "Master"]
               ["Write Replica" "->" "Master"]
               ["Read Replica 1" "--" "Read Replica 2"]
               ["Read Replica 1" "--" "Read Replica 2" "Kept in sync"]
               ["be" "Backend"]
               ["fe" "Frontend"]
               [:comment "This would create new shapes"]
               ["Backend" "->" "Frontend"]
               [:comment "This would define a connection over existing labels"]
               ["be" "->" "fe"]
               ["Write Replica Canada" "<->" "Write Replica Australia"]
               ["Read Replica" "<-" "Master"]
               ["x" "--" "y"]
               ["super long shape id here"
                "->"
                "super long shape id even longer here"]
               ["Database" "->" "S3" "backup"]
               ["Database" "->" "S3"]
               ["Database" "->" "S3" "backup"]
               ["High Mem Instance"
                "->"
                "EC2"
                "<-"
                "High CPU Instance"
                "Hosted By"]
               ["Stage One" "->" "Stage Two" "->" "Stage Three" "->" "Stage Four"]
               ["Stage Four" "->" "Stage One" "repeat"]
               ["a"
                "The best way to avoid responsibility is to say, \"I've got responsibilities\""]
               ["b" "Whether weary or unweary, O man, do not rest"]
               ["c"
                "I still maintain the point that designing a monolithic kernel in 1991 is a"]
               ["a"
                "->"
                "b"
                "To err is human, to moo bovine"
                {"source-arrowhead" 1,
                 "target-arrowhead" {"shape" "diamond", "label" "*"}}]
               ["b"
                "<->"
                "c"
                "\"Reality is just a crutch for people who can't handle science fiction\""
                {"source-arrowhead.label" 1,
                 "target-arrowhead"
                 {"shape" "diamond", "style.filled" true, "label" "*"}}]
               ["d"
                "A black cat crossing your path signifies that the animal is going somewhere"]
               ["d" "->" "a" "->" "c"]))))))


(deftest styles
  (testing "Styles"
    (let [d2 (slurp "test/dictim/d2/samples/style.d2")
          dict (p/dictim d2)]
      (is (= 1 (num-parses d2)))
      (is (= true (v/all-valid? dict :d2))))))


(deftest sql
  (testing "Sql Tables"
    (let [d2 (slurp "test/dictim/d2/samples/sql.d2")
          dict (p/dictim d2)]
      (is (= 1 (num-parses d2)))
      (is (= true (v/all-valid? dict :d2)))
      (is (= dict
             '(["my_table"
                {"shape" "sql_table"}
                ["id" "int" {"constraint" "primary_key"}]
                ["last_updated" "timestamp with time zone"]]
               ["objects"
                {"shape" "sql_table"}
                ["id" "int" {"constraint" "primary_key"}]
                ["disk" "int" {"constraint" "foreign_key"}]
                ["json" "jsonb" {"constraint" "unique"}]
                ["last_updated" "timestamp with time zone"]]
               ["disks"
                {"shape" "sql_table"}
                ["id" "int" {"constraint" "primary_key"}]]
               ["objects.disk" "->" "disks.id"]
               ["cloud"
                ["disks"
                 {"shape" "sql_table"}
                 ["id" "int" {"constraint" "primary_key"}]]
                ["blocks"
                 {"shape" "sql_table"}
                 ["id" "int" {"constraint" "primary_key"}]
                 ["disk" "int" {"constraint" "foreign_key"}]
                 ["blob" "blob"]]
                ["blocks.disk" "->" "disks.id"]
                ["AWS S3 Vancouver" "->" "disks"]]))))))


(deftest sequence-digrams
  (testing "Sequence Diagrams"
    (let [d2 (slurp "test/dictim/d2/samples/sequence.d2")
          dict (p/dictim d2)]
      (is (= 1 (num-parses d2)))
      (is (= true (v/all-valid? dict :d2)))
      (is (= dict
             '({"direction" "right"}
               ["Before and after becoming friends"
                ["2007"
                 "Office chatter in 2007"
                 {"shape" "sequence_diagram"}
                 ["alice" "Alice"]
                 ["bob" "Bobby"]
                 ["awkward small talk"
                  ["alice" "->" "bob" "uhm, hi"]
                  ["bob" "->" "alice" "oh, hello"]
                  ["icebreaker attempt"
                   ["alice" "->" "bob" "what did you have for lunch?"]]
                  ["unfortunate outcome" ["bob" "->" "alice" "that's personal"]]]]
                ["2012"
                 "Office chatter in 2012"
                 {"shape" "sequence_diagram"}
                 ["alice" "Alice"]
                 ["bob" "Bobby"]
                 ["alice" "->" "bob" "Want to play with ChatGPT?"]
                 ["bob" "->" "alice" "Yes!"]
                 ["bob" "->" "alice.play" "Write a play..."]
                 ["alice.play" "->" "bob.play" "about 2 friends..."]
                 ["bob.play" "->" "alice.play" "who find love..."]
                 ["alice.play" "->" "bob.play" "in a sequence diagram"]]
                ["2007" "->" "2012" "Five\\nyears\\nlater"]]
               ["Office chatter"
                {"shape" "sequence_diagram"}
                ["alice" "Alice"]
                ["bob" "Bobby"]
                ["awkward small talk"
                 ["alice" "->" "bob" "uhm, hi"]
                 ["bob" "->" "alice" "oh, hello"]
                 ["icebreaker attempt"
                  ["alice" "->" "bob" "what did you have for lunch?"]]
                 ["unfortunate outcome" ["bob" "->" "alice" "that's personal"]]]]
               {"shape" "sequence_diagram"}
               ["alice" "->" "bob" "What does it mean\\nto be well-adjusted?"]
               ["bob"
                "->"
                "alice"
                "The ability to play bridge or\\ngolf as if they were games."]
               {"shape" "sequence_diagram"}
               [:comment
                "Remember that semicolons allow multiple objects to be defined in one line"]
               [:comment "Actors will appear from left-to-right as a, b, c, d..."]
               [:list ["a"] ["b"] ["c"] ["d"]]
               [:comment "... even if the connections are in a different order"]
               ["c" "->" "d"]
               ["d" "->" "a"]
               ["b" "->" "d"]))))))


(deftest interactive
  (testing "Interactive features"
    (let [d2 (slurp "test/dictim/d2/samples/interactive.d2")
          dict (p/dictim d2)]
      (is (= 1 (num-parses d2)))
      (is (= true (v/all-valid? dict :d2)))
      (is (= dict
             '(["x"
                {"tooltip" "Total abstinence is easier than perfect moderation "}]
               ["y"
                {"tooltip"
                 "Gee, I feel kind of LIGHT in the head now,\\nknowing I can't make my satellite dish PAYMENTS! "}]
               ["x" "->" "y"]
               ["x" "I'm a Mac" {"link" "https://apple.com"}]
               ["y" "And I'm a PC" {"link" "https://microsoft.com"}]
               ["x" "->" "y" "gazoontit"]))))))


(deftest textandcode
  (testing "Text and Code"
    (let [d2 (slurp "test/dictim/d2/samples/textandcode.d2")
          dict (p/dictim d2)]
      (is (= 1 (num-parses d2)))
      (is (= true (v/all-valid? dict :d2)))
      (is (= dict
             '(["explanation"
                "|md\n  # I can do headers\n  - lists\n  - lists\n\n  And other normal markdown stuff\n|"]
               ["my_code"
                "|||ts\n  declare function getSmallPet(): Fish | Bird;\n  const works = (a > 1) || (b < 2)\n|||"]
               ["my_code"
                "|`ts\n  declare function getSmallPet(): Fish | Bird;\n  const works = (a > 1) || (b < 2)\n`|"]
               ["amscd plugin"
                ["ex"
                 "|tex\n\\\\begin{CD} B @>{\\\\text{very long label}}>> C S^{{\\\\mathcal{W}}_\\\\Lambda}\\\\otimes T @>j>> T\\\\\\\\ @VVV V \\\\end{CD}\n|"]]
               ["multilines"
                ["ex"
                 "|tex\n\\\\displaylines{x = a + b \\\\\\\\ y = b + c}\n\\\\sum_{k=1}^{n} h_{k} \\\\int_{0}^{1} \\\\bigl(\\\\partial_{k} f(x_{k-1}+t h_{k} e_{k}) -\\\\partial_{k} f(a)\\\\bigr) \\\\,dt\n|"]]
               ["title"
                "A winning strategy"
                {"shape" "text",
                 "near" "top-center",
                 "style" {"font-size" 55, "italic" true}}]
               ["poll the people" "->" "results"]
               ["results" "->" "unfavorable" "->" "poll the people"]
               ["results" "->" "favorable" "->" "will of the people"]))))))


(deftest fill-pattern
  (testing "fill-pattern can be parsed"
    (is (= (p/dictim
            "1 -> 2:  {\n  style:  {\n    fill-pattern: lines\n  }\n}")
           '(["1" "->" "2" {"style" {"fill-pattern" "lines"}}])))))


(deftest classes
  (testing "Software Classes"
    (let [d2 (slurp "test/dictim/d2/samples/classes.d2")
          dict (p/dictim d2)]
      (is (= 1 (num-parses d2)))
      (is (= true (v/all-valid? dict :d2)))
      (is (= dict
             '(["MyClass"
                {"shape" "class"}
                ["field" "\"[]string\""]
                ["method(a uint64)" "(x, y int)"]]
               ["D2 Parser"
                {"shape" "class"}
                [:comment "Default visibility is + so no need to specify."]
                ["+reader" "io.RuneReader"]
                ["readerPos" "d2ast.Position"]
                [:comment "Private field."]
                ["-lookahead" "\"[]rune\""]
                [:comment "Protected field."]
                [:comment
                 "We have to escape the # to prevent the line from being parsed as a comment."]
                ["\\#lookaheadPos" "d2ast.Position"]
                ["+peek()" "(r rune, eof bool)"]
                ["rewind()"]
                ["commit()"]
                ["\\#peekn(n int)" "(s string, eof bool)"]]
               ["\"github.com/terrastruct/d2parser.git\"" "--" "D2 Parser"])))))
  (testing "d2 Classes"
    (let [d2 (slurp "test/dictim/d2/samples/classes3.d2")
          dict (p/dictim d2)]
      (is (= 1 (num-parses d2)))
      (is (= true (v/all-valid? dict :d2)))
      (is (= dict
             '({"direction" "right"}
               {"classes"
                {"load balancer"
                 {"label" "load\\nbalancer",
                  "width" 100,
                  "height" 200,
                  "style"
                  {"stroke-width" 0,
                   "fill" "\"#44C7B1\"",
                   "shadow" true,
                   "border-radius" 5}},
                 "unhealthy"
                 {"style" {"fill" "\"#FE7070\"", "stroke" "\"#F69E03\""}}}}
               ["web traffic" "->" "web lb"]
               {"web lb.class" "load balancer"}
               ["web lb" "->" "api1"]
               ["web lb" "->" "api2"]
               ["web lb" "->" "api3"]
               {"api2.class" "unhealthy"}
               ["api1" "->" "cache lb"]
               ["api3" "->" "cache lb"]
               {"cache lb.class" "load balancer"}))))))



(deftest globs
  (testing (str "glob in dotted word but not in end position -> attribute \n"
                "glob in dotted word in end position -> shape or container.")
    (let [d2 (slurp "test/dictim/d2/samples/globs.d2")
          dict (p/dictim d2)]
      (is (= 1 (num-parses d2)))
      (is (= true (v/all-valid? dict :d2)))
      (is (= dict
             '(["foods"
                ["pizzas"
                 ["cheese"]
                 ["sausage"]
                 ["pineapple"]
                 {"*.shape" "circle"}]
                ["humans" ["john"] ["james"] {"*.shape" "person"}]
                ["humans.*" "->" "pizzas.pineapple" "eats"]])))))
  (testing "recursive globs"
    (let [d2 "a:   {\n  b:   {\n    c\n  }\n}\n**.style.border-radius: 7"
          dict (p/dictim d2)]
      (is (= 1 (num-parses d2)))
      (is (= true (v/all-valid? dict :d2)))
      (is (= dict
             '(["a" ["b" ["c"]]] {"**.style.border-radius" 7})))))
  (testing "can parse & (filters what globs can target"
    (let [d2 "bravo team.shape: person
              charlie team.shape: person
              command center.shape: cloud
              hq.shape: rectangle

              *: {
                &shape: person
                style.multiple: true
              }"
          dict (p/dictim d2)]
      (is (= 1 (num-parses d2)))
      (is (= true (v/all-valid? dict :d2)))
      (is (= dict
             '({"bravo team.shape" "person"}
               {"charlie team.shape" "person"}
               {"command center.shape" "cloud"}
               {"hq.shape" "rectangle"}
               {"*" {"&shape" "person" "style.multiple" true}}))))))


(deftest vars
  (testing "parse vars and substitutions in shape & ctr labels"
    (let [d2 "direction: right
              vars: {
                server-name: Cat
              }
              server2: ${ab} ${server-name}-2 {
                buttons
              }
              server1 <-> server2"
          dict (p/dictim d2)]
      (is (= 1 (num-parses d2)))
      (is (= true (v/all-valid? dict :d2)))
      (is (= dict
             '({"direction" "right"}
               {"vars" {"server-name" "Cat"}}
               ["server2" "${ab} ${server-name}-2" ["buttons"]]
               ["server1" "<->" "server2"])))))
  (testing "parse substitutions in attribute value"
    (let [d2 "vars: {
                primaryColors: {
                  button: {
                    active: \"#4baae5\"
                    border: black
                  }
                }
              }

              button: {
                width: 100
                height: 40
                style: {
                  border-radius: 5
                  fill: ${primaryColors.button.active}
                  stroke: ${primaryColors.button.border}
                }
              }"
          dict (p/dictim d2)]
      (is (= 1 (num-parses d2)))
      (is (= true (v/all-valid? dict :d2)))
      (is (= dict
             '({"vars"
                {"primaryColors"
                 {"button" {"active" "\"#4baae5\"", "border" "black"}}}}
               ["button"
                {"width" 100,
                 "height" 40,
                 "style"
                 {"border-radius" 5,
                  "fill" "${primaryColors.button.active}",
                  "stroke" "${primaryColors.button.border}"}}])))))
  (testing "single quoted substitutions (which are bypassed by d2)"
    (let [d2 "direction: right
              vars: {
                names: John and Joyce
              }
              a -> b: 'Send field ${names}'"
          dict (p/dictim d2)]
      (is (= 1 (num-parses d2)))
      (is (= true (v/all-valid? dict :d2)))
      (is (= dict
             '({"direction" "right"}
               {"vars" {"names" "John and Joyce"}}
               ["a" "->" "b" "'Send field ${names}'"])))))
  (testing "nested vars under d2-config key"
    (let [d2 "vars: {
                d2-config: {
                  theme-id: 4
                  dark-theme-id: 200
                  pad: 0
                  center: true
                  sketch: true
                  layout-engine: elk
                }
              }

              direction: right
              x -> y"
          dict (p/dictim d2)]
      (is (= 1 (num-parses d2)))
      (is (= true (v/all-valid? dict :d2)))
      (is (= dict
             '({"vars"
                {"d2-config"
                 {"theme-id" 4,
                  "dark-theme-id" 200,
                  "pad" 0,
                  "center" true,
                  "sketch" true,
                  "layout-engine" "elk"}}}
               {"direction" "right"}
               ["x" "->" "y"]))))))


(deftest connection-references
  (testing "connection references can be parsed."
    (let [d2 "lady 1
              lady 2

              barbie

              lady 1 -> barbie: hi barbie
              lady 2 -> barbie: hi barbie

              (lady* -> barbie)[*].style.stroke: pink"
          dict (p/dictim d2)]
      (is (= 1 (num-parses d2)))
      (is (= true (v/all-valid? dict :d2)))
      (is (= dict
             '(["lady 1"]
               ["lady 2"]
               ["barbie"]
               ["lady 1" "->" "barbie" "hi barbie"]
               ["lady 2" "->" "barbie" "hi barbie"]
               {["lady*" "->" "barbie" ["*"]] {"style.stroke" "pink"}}))))))


(deftest nulls
  (testing "connection references can be nulled."
    (let [d2 "(a -> b)[0]: null"
          dict (p/dictim d2)]
      (is (= 1 (num-parses d2)))
      (is (= true (v/all-valid? dict :d2)))
      (is (= dict
             '({["a" "->" "b" [0]] nil})))))
  (testing "shapes/ containers can be nulled."
    (let [d2 "pig: null {piglet}"
          dict (p/dictim d2)]
      (is (= 1 (num-parses d2)))
      (is (= true (v/all-valid? dict :d2)))
      (is (= dict
             '(["pig" nil ["piglet"]])))))
  (testing "single-connections can be nulled."
    (let [d2 "x -> y: null"
          dict (p/dictim d2)]
      (is (= 1 (num-parses d2)))
      (is (= true (v/all-valid? dict :d2)))
      (is (= dict
             '(["x" "->" "y" nil])))))
  (testing "multiple-connections can be nulled."
    (let [d2 "x -> y <- z: null"
          dict (p/dictim d2)]
      (is (= 1 (num-parses d2)))
      (is (= true (v/all-valid? dict :d2)))
      (is (= dict
             '(["x" "->" "y" "<-" "z" nil]))))))


(deftest layers-scenarios-steps
  (testing "layers can be parsed"
    (let [d2 "aShape: I'm a Shape\nlayers:   {\n  tiktok:   {\n    what: A messaging app\n  }\n  facebook:   {\n    was: A legacy messaging app\n  }\n}"
          dict (p/dictim d2)]
      (is (= 1 (num-parses d2)))
      (is (= true (v/all-valid? dict :d2)))
      (is (= dict
             '(["aShape" "I'm a Shape"]
               ["layers"
                ["tiktok" ["what" "A messaging app"]]
                ["facebook" ["was" "A legacy messaging app"]]])))))
  (testing "scenarios can be parsed"
    (let [d2 "aShape: I'm a Shape\nscenarios:   {\n  tiktok:   {\n    what: A messaging app\n  }\n  facebook:   {\n    was: A legacy messaging app\n  }\n}"
          dict (p/dictim d2)]
      (is (= 1 (num-parses d2)))
      (is (= true (v/all-valid? dict :d2)))
      (is (= dict
             '(["aShape" "I'm a Shape"]
               ["scenarios"
                ["tiktok" ["what" "A messaging app"]]
                ["facebook" ["was" "A legacy messaging app"]]])))))
  (testing "steps can be parsed"
    (let [d2 "aShape: I'm a Shape\nsteps:   {\n  tiktok:   {\n    what: A messaging app\n  }\n  facebook:   {\n    was: A legacy messaging app\n  }\n}"
          dict (p/dictim d2)]
      (is (= 1 (num-parses d2)))
      (is (= true (v/all-valid? dict :d2)))
      (is (= dict
             '(["aShape" "I'm a Shape"]
               ["steps"
                ["tiktok" ["what" "A messaging app"]]
                ["facebook" ["was" "A legacy messaging app"]]]))))))


(deftest inner-lists
  (testing "inner-lists can be parsed in attr vals"
    (let [d2 "test: {direction: [a; ...${ab}]}"
          dict (p/dictim d2)]
      (is (= 1 (num-parses d2)))
      (is (= true (v/all-valid? dict :d2)))
      (is (= dict
             '(["test" {"direction" ["a" "...${ab}"]}]))))))
