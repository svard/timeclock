(ns net.svard.timeclock.account
  (:refer-clojure :exclude [find])
  (:require [monger.collection :as mc]
            [crypto.password.scrypt :as password])
  (:import [org.bson.types ObjectId]))

(defonce coll "accounts")

(defn create [db account]
  (as-> account $
    (update-in $ [:password] password/encrypt)
    (mc/insert-and-return db coll $)))

(defn find [db username]
  (mc/find-one-as-map db coll {:username username}))
