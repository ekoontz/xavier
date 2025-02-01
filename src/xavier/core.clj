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

