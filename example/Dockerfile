FROM tuanloc/alpine:java11

WORKDIR /app

COPY target/todo-app-example-1.0.0-jar-with-dependencies.jar /app/app.jar

RUN mkdir -p /app/data

ENTRYPOINT [ \
    "java", \
    "-server", \
    "-Duser.timezone=Asia/Ho_Chi_Minh", \
    "-Dfile.encoding=UTF8", \
    "-Xlog:gc*:file=data/gc.log:time,uptime,level,tags:filecount=5,filesize=10m", \
    "-cp", \
    "app.jar" \
]
