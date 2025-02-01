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

           ;; if user-input is a prefix of any of the targets, increment score:
           (if (seq correct-prefixes)
             {:score (inc current-score)
              :correct-prefix (first correct-prefixes)}

             ;; otherwise, decrement score:
             {:score (dec current-score)
              :correct-prefix (->> user-input butlast (clojure.string/join ""))}))))

(defn server-response [user-request]
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
        (dissoc :targets))))

(def initial-rr
  ["GET /question/751"
   question-response])

(def rr-pair-1
  (let [user-request
        {:user-input "l"
         :encrypted encrypted-0}]
    [user-request
     (server-response user-request)]))

(def rr-pair-2
  (let [user-request
        {:user-input "le"
         :encrypted (-> rr-pair-1 second :encrypted)}]
    [user-request
     (server-response user-request)]))

(def rr-pair-3
  (let [user-request
        {:user-input "lej"
         :encrypted (-> rr-pair-2 second :encrypted)}]
    [user-request
     (server-response user-request)]))

(def rr-pair-4
  (let [user-request
        {:user-input "lei"
         :encrypted (-> rr-pair-3 second :encrypted)}]
    [user-request
     (server-response user-request)]))

(def rr-pair-5
  (let [user-request
        {:user-input "lei "
         :encrypted (-> rr-pair-4 second :encrypted)}]
    [user-request
     (server-response user-request)]))

(def rr-pair-6
  (let [user-request
        {:user-input "lei f"
         :encrypted (-> rr-pair-5 second :encrypted)}]
    [user-request
     (server-response user-request)]))

(def requests-and-responses
  (->>
   [initial-rr rr-pair-1 rr-pair-2 rr-pair-3 rr-pair-4 rr-pair-5 rr-pair-6]
   (map (fn [x]
          {:request (first x)
           :response (second x)}))))

