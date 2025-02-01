(ns xavier.core
  (:require [lock-key.core :as lock-key]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(defn foo []
  (+ 42 1))

;; TODO: use environmental variable
(def password "sUzIbwS6aEq1J0X6mLGh")

(defn encrypt [plaintext-input]
  (lock-key/encrypt-as-base64 plaintext-input password))

(defn decrypt [encrypted-input]
  (lock-key/decrypt-from-base64 encrypted-input password))

(defn demo []
  (-> "hello how are you??" encrypt decrypt))


;; lei dorme -> she sleeps

(def question-response
  {:question "she sleeps"
   :encrypted (encrypt (str {:targets "[lei dorme]"
                             :score 0}))})

(defn increment-score [encrypted]
  (let [decrypted (-> encrypted decrypt read-string)]
    (-> decrypted
        (merge {:score (-> decrypted :score inc)})
        str
        encrypt)))

(defn decrement-score [encrypted]
  (let [decrypted (-> encrypted decrypt read-string)]
    (-> decrypted
        (merge {:score (-> decrypted :score dec)})
        str
        encrypt)))

(def encrypted (-> question-response :encrypted))

(def requests-and-responses
  (concat
   [[
     {:keystroke "l"
      :encrypted encrypted}
     {:correct-prefix "l"
      :encrypted (increment-score encrypted)}]]

   (let [encrypted (increment-score encrypted)]
     (concat
      [[
        {:keystroke "le"
         :encrypted encrypted}
        {:correct-prefix "le"
         :encrypted (increment-score encrypted)}]]

      (let [encrypted (increment-score encrypted)]
        [[
          {:keystroke "lej"
           :encrypted encrypted}
          {:correct-prefix "le"
           :encrypted (decrement-score encrypted)}]])))))
