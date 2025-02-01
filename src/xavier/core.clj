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
   :targets "[lei dorme]"
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


  (merge {:targets targets}
         (if (seq (remove false? (map (fn [target]
                                        (if (clojure.string/starts-with? target user-input)
                                          user-input false))
                                      targets)))
           {:score (inc current-score)
            :correct-prefix user-input}
           {:score (dec current-score)
            :correct-prefix (->> user-input butlast (clojure.string/join ""))})))

(def rr-pair-1
  [
   ;; request 1
   {:user-input "l"
    :encrypted encrypted-0}
   
   ;; response 1
   (merge (evaluate "l" ["lei dorme"] 0)
          {:encrypted (encrypt (str (evaluate "l" ["lei dorme"] 0)))
           :decrypted (decrypt (encrypt (str (evaluate "l" ["lei dorme"] 0))))})])

(def rr-pair-2
  [
   ;; request 2
   {:user-input "le"
    :encrypted (-> rr-pair-1 second :encrypted)}
   
   ;; response 2
   {:correct-prefix "le"
    :encrypted encrypted-2}])

(def rr-pair-3
  [
   ;; request 3
   {:user-input "lej"
    :encrypted encrypted-2}
   ;; response 3
   {:correct-prefix "le"
    :encrypted encrypted-3}])

(def initial-rr
  [{:http-request "GET /question/751"}
   question-response])

(def requests-and-responses
  [initial-rr rr-pair-1 rr-pair-2 rr-pair-3])
