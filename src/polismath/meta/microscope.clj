;; Copyright (C) 2012-present, Polis Technology Inc. This program is free software: you can redistribute it and/or  modify it under the terms of the GNU Affero General Public License, version 3, as published by the Free Software Foundation. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details. You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.

(ns polismath.meta.microscope
  (:require [polismath.conv-man :as cm]
            [polismath.components.env :as env]
            [polismath.components.postgres :as db]
            [polismath.math.conversation :as conv]
            [polismath.math.named-matrix :as nm]
            [polismath.utils :as utils]
            [clojure.tools.trace :as tr]
            [clojure.tools.logging :as log]
            [clojure.newtools.cli :refer [parse-opts]]
            [com.stuartsierra.component :as component]
            [plumbing.core :as pc]
            [korma.core :as ko]
            [korma.db :as kdb]))
;
;
;
;(defrecord Microscope [postgres mongo conversation-manager config]
;  component/Lifecycle
;  ;; Don't know if there will really be any important state here;
;  ;; Seems like an anti-pattern and like we should just be operating on some base-system...
;  (start [this] this)
;  (stop [this] this))
;
;;; XXX Should really move to db
(defn get-zid-from-zinvite
  [{:as system :keys [postgres]} zinvite]
  (->
    (kdb/with-db (:db-spec postgres)
      (ko/select "zinvites"
        (ko/fields :zid :zinvite)
        (ko/where {:zinvite zinvite})))
    first
    :zid))

(comment
  (require '[polismath.runner :as runner :refer [system]])
  (get-zid-from-zinvite system "7scufp")
  :end-example-comment)


(defn recompute
  [{:as system :keys [conversation-manager postgres]} & {:keys [zid zinvite] :as args}]
  (assert (utils/xor zid zinvite))
  (let [zid        (or zid (get-zid-from-zinvite system zinvite))
        new-votes  (db/conv-poll postgres zid 0)]
    (log/info "Running a recompute on zid:" zid "(zinvite:" zinvite ")")
    (cm/queue-message-batch! conversation-manager :votes zid new-votes)
    (cm/add-listener!
      conversation-manager
      :complete-watch
      (fn [new-conv]
        (log/info "Done recomputing conv")))))

(comment
  (require '[polismath.runner :as runner :refer [system]])
  (get-zid-from-zinvite system "7scufp")
  (recompute system :zinvite "7scufp")
  :end-example-comment)

;
;
;(defn kw->int
;  [kw]
;  (-> kw
;      (str)
;      (clojure.string/replace ":" "")
;      (Integer/parseInt)))
;
;
;(defn load-conv
;  [& {:keys [zid zinvite env-overrides] :or {env-overrides {}} :as args}]
;  (assert (utils/xor zid zinvite))
;  (let [zid (or zid (get-zid-from-zinvite zinvite))]
;    (env/with-env-overrides env-overrides
;      (->
;        (db/load-conv zid)
;        ;; This should be ok here right?
;        (cm/restructure-mongo-conv)
;        (update-in
;          [:repness]
;          (partial pc/map-keys kw->int))))))
;
;
;(defn replay-conv-update
;  "Can be run as a shell command on a error file to replay what happened."
;  [filename]
;  (let [data (conv/load-conv-update filename)
;        {:keys [conv votes opts]} data
;        {:keys [rating-mat base-clusters pca]} conv]
;    (println "Loaded conv:" filename)
;    (println "Dimensions:" (count (nm/rownames rating-mat)) "x" (count (nm/colnames rating-mat)))
;    (conv/conv-update conv votes)))
;
;
;(def cli-options
;  [["-z" "--zid ZID" "ZID on which to do a rerun" :parse-fn #(Integer/parseInt %)]
;   ["-Z" "--zinvite ZINVITE" "ZINVITE code on which to perform a rerun"]
;   ["-r" "--recompute" "If set, will run a full recompute"]])
;
;
;(defn -main
;  [& args]
;  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)
;        conv-actor (utils/apply-kwargs recompute options)
;        done? (atom false)]
;    (add-watch
;      (:conv conv-actor)
;      :shutdown-watch
;      (fn [k r o n]
;        (println n)
;        (swap! done? (fn [_] true))
;        (shutdown-agents)))
;    (loop []
;      (Thread/sleep 1000)
;      (when-not @done?
;        (recur)))))
;

