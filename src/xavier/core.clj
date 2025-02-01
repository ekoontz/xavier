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

(def question-and-answer-structure
  ;; when the user GETs /question/751, they get back:
  {:question-id 42
   :question "she sleeps"
   :targets ["lei dorme"]
   :correct-prefix ""
   :score 0})

(def question-response
  (-> question-and-answer-structure
      (dissoc :targets)
      (merge {:encrypted (encrypt (str question-and-answer-structure))})))

(defn increment-score [encrypted]
  (let [decrypted (-> encrypted decrypt read-string)]
    (-> decrypted
        (merge {:score (-> decrypted :score inc)}))))

(defn decrement-score [encrypted]
  (let [decrypted (-> encrypted decrypt read-string)]
    (-> decrypted
        (merge {:score (-> decrypted :score dec)}))))

(def encrypted-0 (-> question-response :encrypted))
(def encrypted-1 (-> encrypted-0 increment-score str encrypt))
(def encrypted-2 (-> encrypted-1 increment-score str encrypt))
(def encrypted-3 (-> encrypted-2 decrement-score str encrypt))

(defn evaluate [user-input targets current-score]
  ;; if user-input is a prefix of any of the targets, increment score; otherwise, decrement score.

  (let [correct-prefixes
        (->>
         (remove false? (map (fn [target]
                               (if (clojure.string/starts-with? target user-input)
                                 user-input false))
                             targets))
         (sort (fn [a b] (> (count a) (count b)))))]
    (merge {:targets targets}
           (if (seq correct-prefixes)
             {:score (inc current-score)
              :correct-prefix (first correct-prefixes)}
             {:score (dec current-score)
              :correct-prefix (->> user-input butlast (clojure.string/join ""))}))))

(defn server-response [user-input targets score]
  (evaluate user-input targets score))

(def initial-rr
  [{:http-request "GET /question/751"}
   question-response])

(def rr-pair-1
  (let [user-request
        {:user-input "l"
         :encrypted encrypted-0}]
    [
     ;; request 1
     user-request

     ;; response 1
     (let [
           ;; 1. get from the user input two params: :user-input and :encrypted
           {user-input :user-input
            encrypted :encrypted} user-request

           ;; 2. decrypt the correct answers and score from the user's :encrypted param:
           {targets :targets
            score :score}
           (-> encrypted decrypt read-string)

           ;; 3. evaluate the user's input against the correct answers:
           evaluation (evaluate user-input targets score)]

       ;; create the response to be sent back to the user:
       (-> evaluation
           (merge
            {:encrypted (-> evaluation str encrypt)})
           (dissoc :targets)))]))

(def rr-pair-2
  (let [user-request
        {:user-input "le"
         :encrypted (-> rr-pair-1 second :encrypted)}]
    [
     ;; request 2
     user-request

     ;; response 2
     (let [
           ;; 1. get from the user input two params: :user-input and :encrypted
           {user-input :user-input
            encrypted :encrypted} user-request
           
           ;; 2. decrypt the correct answers and score from the user's :encrypted param:
           {targets :targets
            score :score}
           (-> encrypted decrypt read-string)

           ;; 3. evaluate the user's input against the correct answers:
           evaluation (evaluate user-input targets score)
           ]

       ;; create the response to be sent back to the user:
       (-> evaluation
           (merge
            {:encrypted (-> evaluation str encrypt)})
           (dissoc :targets)
           (merge {:score (-> evaluation :score)})))]))

(def rr-pair-3
  (let [user-request
        {:user-input "lej"
         :encrypted (-> rr-pair-2 second :encrypted)}]
    [
     ;; request 2
     user-request

     
     ;; response 2
     (let [
           ;; 1. get from the user input two params: :user-input and :encrypted
           {user-input :user-input
            encrypted :encrypted} user-request
           
           ;; 2. decrypt the correct answers and score from the user's :encrypted param:
           {targets :targets
            score :score}
           (-> encrypted decrypt read-string)

           ;; 3. evaluate the user's input against the correct answers:
           evaluation (evaluate user-input targets score)]

       ;; create the response to be sent back to the user:
       (-> evaluation
           (merge
            {:encrypted (-> evaluation str encrypt)})
           (dissoc :targets)))]))

(def requests-and-responses
  [initial-rr rr-pair-1 rr-pair-2 rr-pair-3])
