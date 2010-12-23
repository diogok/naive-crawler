(ns naive-crawler.core
    (:use clojure.contrib.io)
    (:use clojure.contrib.base64))

    (defn safe [fun]
     "Safe call"
      (try (fun) (catch Exception e (do false))))

    (defn make-url [base path]
     "Make a absolute URL taking the current url
     and the next url. Return is a String"
     (or (safe #(.toString (java.net.URL. (java.net.URL. base) path))) base))

    (defn find-links [page]
     "Return the links found on a page as a list"
     (map #(-> % rest second) (re-seq #"href=('|\")([^'\"]+)('|\")" page)) )

    (defn find-urls [base page]
     "Return the aboslute urls for the page at base url"
     (map #(make-url base %1) (find-links page)))

    (defn same-domain? [current next]
     "Return true if both urls are from same domain"
     (= (.getHost (java.net.URL. current)) (.getHost (java.net.URL. next)))) 

    (defn make-db [db]
     "Start an empy database"
     (do (.mkdirs (file-str db)) {:db db :pages (atom {})}))

    (defn insert [db url]
     "Insert an url on the db, if not there already"
     (let [file (file-str (:db db) "/" (encode-str url))] 
       (swap! (:pages db) (fn [pages]
          (if (nil? (get pages url))
            (assoc pages url file) pages)))))

    (defn get-it [{db :pages} url]
     "Get the url from db"
     (slurp* (get @db url))) 

    (defn non-crawled [{ db :pages }]
     "Return urls not crawled"
     (map key (filter #(not ( .exists (val %1) )) @db)))

    (defn set-content [{ db :pages } page content]
     "Set the content of a page on the db"
     (spit (get @db page) content))

    (defn save-page [db page]
     "Get the content of page, save it and add it's urls to be crawled"
     (let [content (or (safe #(slurp* page)) "ERROR")]
      (set-content db page content)
      (dorun (map #(insert db %1) (filter (partial same-domain? page) (find-urls page content))))))

    (defn start [url path] 
     "Start the crawler"
     (let [db (make-db path)]
      (insert db url) 
      (while (not (empty? (non-crawled db)))
       (dorun (pmap #(save-page db %1) (non-crawled db))))
      (deref (db :pages)))) 

    (defn -main 
     ([url]    (start url "pages")) 
     ([url db] (start url db)))
