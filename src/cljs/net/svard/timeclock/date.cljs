(ns ^:figwheel-always net.svard.timeclock.date
  (:require [cljsjs.moment]))

(defprotocol ISO8601DateFormat
  (-to-date-string [this])
  (-to-time-string [this]))

(defprotocol IDateUnit
  (-inc [this])
  (-dec [this])
  (-map [this])
  (-unixtimestamp [this]))

(deftype Date [moment]
  IDateUnit
  (-inc [this]
    (.add (.-moment this) 1 "w")
    this)
  (-dec [this]
    (.subtract (.-moment this) 1 "w")
    this)
  (-map [this]
    {:year (.year (.-moment this)) :week (.isoWeek (.-moment this))})
  (-unixtimestamp [this]
    (.valueOf (.-moment this))) 
  IEquiv
  (-equiv [this other] 
    (and (instance? Date other)
      (= (:year (-map this)) (:year (-map other)))
      (= (:week (-map this)) (:week (-map other))))) 
  ISO8601DateFormat
  (-to-date-string [this]
    (.format (.-moment this) "YYYY-MM-DD"))
  (-to-time-string [this] 
    (.format (.-moment this) "HH:mm:ss")))

(defn new-date
  ([]
   (->Date (js/moment)))
  ([timestamp]
   (->Date (js/moment timestamp))))

(defn print-date [date]
  (-to-date-string date))

(defn print-time [date]
  (-to-time-string date))

(defn print-date-time [date]
  (str (print-date date) " " (print-time date)))

(defn second->hour [second]
  (let [hours (/ second 3600)]
    (-> (.round js/Math (* hours 100))
        (/ 100))))

(defn hour->second [hour]
  (* hour 3600))

(defn weeks-in-year [year]
  (.isoWeeksInYear (js/moment (str year "-01-01"))))

(defn year [date]
  (:year (-map date)))

(defn week [date]
  (:week (-map date)))

(defn incr-query-params [{:keys [year week]}]
  (if (> (inc week) (weeks-in-year year))
    {:year (inc year) :week 1}
    {:year year :week (inc week)}))

(defn decr-query-params [{:keys [year week]}]
  (if (< (dec week) 1)
    {:year (dec year) :week (weeks-in-year (dec year))}
    {:year year :week (dec week)}))

(defn incr-week [date]
  (-inc date))

(defn decr-week [date]
  (-dec date))

(defn subtract [d1 d2]
  (let [s1 (/ (-unixtimestamp d1) 1000)
        s2 (/ (-unixtimestamp d2) 1000)]
    (- s1 s2)))
