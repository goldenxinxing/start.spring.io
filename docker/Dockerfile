FROM ampregistry:5000/sng-biz-base-alpine:2.0.1

ENV TZ "Asia/Shanghai"
ENV LANG th_TH.UTF-8
ENV LC_ALL th_TH.UTF-8
ENV LANGUAGE th_TH:th

WORKDIR $PRO_PATH

ADD start-site-exec.jar $PRO_PATH

CMD java -Xms2g -Xmx2g -Xmn1g -server -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=70 -XX:+CMSClassUnloadingEnabled -XX:+PrintGCTimeStamps -XX:+ExplicitGCInvokesConcurrentAndUnloadsClasses -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+UseCMSInitiatingOccupancyOnly -jar start-site-exec.jar