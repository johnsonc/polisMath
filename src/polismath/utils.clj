(ns polismath.utils
  (:use 
   ; alex-and-georges.debug-repl
        clojure.core.matrix)
  (:require [taoensso.timbre.profiling :as profiling
               :refer (pspy pspy* profile defnp p p*)]
            [clojure.core.matrix :as mat]
            [clojure.tools.trace :as tr]))

(set-current-implementation :vectorz)


(defn xor
  [a b]
  (and
    (or a b)
    (not (and a b))))


(defn gets
  "Like get, but gives a coll mapped from all the keys"
  [m ks & [not-found]]
  (mapv #(get m % not-found) ks))


(defn exit [status msg]
  (println msg)
  (System/exit status))


(defn agree? [n]
  (and
    (not (nil? n))
    (< n 0)))


(defn disagree? [n]
  (and
    (not (nil? n))
    (> n 0)))


(defmacro time2
  [tag & expr]
  `(let [start# (. System (nanoTime))
         ret# ~@expr]
     (println (str (System/currentTimeMillis) " " ~tag " " (/ (double (- (. System (nanoTime)) start#)) 1000000.0) " msecs"))
     ret#))


(defmacro f?>>
  "Modified 'penguin' operator from plumbing.core, where do-it? is a function of the threaded value
  instead of a static value. E.g.: (->> nums (?>> #(even? (count %)) (map inc)))"
  [do-it? & args]
  `(if (~do-it? ~(last args))
     (->> ~(last args) ~@(butlast args))
     ~(last args)))


(defmacro f?>
  "Modified 'penguin' operator from plumbing.core, where do-it? is a function of the threaded value
  instead of a static value. E.g.: (-> n inc (?> even? (* 2)))"
  [arg do-it? & rest]
  `(if (~do-it? ~arg)
     (-> ~arg ~@rest)
     ~arg))


(defn zip [& xss]
  ;;should we redo this like the with-indices below, using a map?
  (if (> (count xss) 1)
    (partition (count xss) (apply interleave xss))
    xss))


(defn map-rest
  "Like map, but the mapper function takes both the item and the rest of the items in the collection,
  letting you operate on each item in comparison with all the others easily"
  [f col]
  (for [i (range (count col))]
    (f (get col i)
       (concat
         (subvec col 0 i)
         (subvec col (inc i))))))


(defn mapv-rest
  "Like map-rest, but returns a vector instead of a lazy seq"
  [f col]
  (vec (map-rest f col)))


(let [greedy? true]
  (defn greedy [iter]
    (if greedy?
      (into [] iter)
      iter)))


(defn greedy-false [iter] iter)


(defn ^long typed-indexof [^java.util.List coll item]
  (.indexOf coll item))


(defmacro endlessly [interval & forms]
  `(doseq [~'x (range)]
     ~@forms
     (Thread/sleep ~interval)))


(defn with-indices [coll]
  (map #(vector %1 %2) (range) coll))


(defn filter-by-index [coll idxs] 
  (greedy-false
  (let [idx-set (set idxs)]
    (->> (with-indices coll)
      (filter #(idx-set (first %)))
      (map second)))
    ))


(defn apply-kwargs
  "Takes a function f, any number of regular args, and a final kw-args argument which will be
  splatted in as a final argument"
  [f & args]
  (apply (apply partial f (butlast args)) (apply concat (last args))))


(defn hash-map-subset
    "Create a new map which is given by subsetting to the given keys (ks)"
    [m ks]
    (let [ks (set ks)]
      (into {}
        (filter
          (fn [[k v]] (ks k))
          m))))


(defn use-debuggers
  "Handy debugging utility for loading in debugging namespaces - doesn't really always work. XXX - maybe
  use Vinyasa?"
  []
  ;(require '[alex-and-georges.debug-repl :as dr])
  (require '[clojure.tools.trace :as tr]))


(defn clst-trace
  ([clsts] (clst-trace "" clsts))
  ([k clsts]
   (println "TRACE" k ":")
   (doseq [c clsts]
     (println "   " c))
   clsts))


