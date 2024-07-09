const Lame = require("node-lame").Lame;
const eureka = require("eureka-js-client").Eureka;
const express = require("express");
const multer = require("multer");

const bodyParser = require("body-parser");
// const { Essentia, EssentiaWASM, EssentiaModel } = require("essentia.js");
// const essentia = new Essentia(EssentiaWASM);

const {Kafka} = require("kafkajs");
const kafka = new Kafka({
    clientId: "essentiajs-service",
    brokers: ["localhost:19092"],
});
const producer = kafka.producer();
producer.connect().then(() => {
    console.log("Connected to Kafka");
});

const app = express();
app.hostName = "192.168.188.28";
const port = 9097;
// Multer configuration
const storage = multer.diskStorage({
    destination: (req, file, cb) => {
        cb(null, "upload/");
    },
    filename: (req, file, cb) => {
        cb(null, file.originalname);
    },
});
const upload = multer({
    storage: storage,
    fileFilter: function (req, file, cb) {
        if (!file.originalname.match(/\.(mp3|wav)$/)) {
            return cb(new Error("Only audio files are allowed!"), false);
        }
        cb(null, true);
    },
});

const client = new eureka({
    instance: {
        app: "ESSENTIAJS-SERVICE",
        instanceId: "ESSENTIAJS-SERVICE",
        hostName: "192.168.188.28",
        ipAddr: "192.168.188.28",
        vipAddress: "ESSENTIAJS-SERVICE",
        statusPageUrl: "http://192.168.188.28:9097/info",
        healthCheckUrl: "http://192.168.188.28:9097/info",
        port: {
            $: port,
            "@enabled": "true",
        },
        dataCenterInfo: {
            name: "MyOwn",
            "@class": "com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo",
        },
    },
    eureka: {
        host: "user:password@localhost",
        port: 8761,
        servicePath: "/eureka/apps/",
    },
});

/**
 * Analysis Funtion
 */
function analyze(rawdata, userName, path) {
    var result = {};
    const buffer = Buffer.concat(rawdata);
    console.log("Analyzing audio");
    const encoder = new Lame({
        output: "buffer",
        raw: true,
    }).setFile(path);
    encoder
        .encode()
        .then(() => {
            // const audio = encoder.getBuffer();
            // const audioVector = essentia.arrayToVector(audio);
            // let beats = essentia.RhythmExtractor(audioVector);
            // let bpm = essentia.RhythmExtractor(audioVector).bpm;
            // let danceability = essentia.Danceability(audioVector).danceability;
            result = {
                requestId: "Jacopo",
                user:"Jacopo",
                genre: ["rock"],
                bpm: "120",
                danceability: "0.5",
            };
            ["analysis-query_params", "audio_analysis"].forEach((topic) => {
                producer
                    .send({
                        topic: topic,
                        messages: [{value: JSON.stringify(result)}],
                    })
                    .then(() => {
                        console.log("Message sent to Kafka");
                    });
            });
            console.log(result);
            return result;
        })
        .catch((error) => {
            console.log("error: " + error);
            result = null;
        })
        .finally(() => {
            console.log("Audio analysis completed");
        });
}

/*
 * Express endpoints
 * */
app.get("/info", (req, res) => {
    res.send("Essentia.js service is running");
});
app.post("/audio/audio_analysis", upload.single("file"), (req, res) => {
    let audioData = [];

    console.table(req.file);
    analyze(audioData, req.file.originalname, req.file.path);
    res.send("Audio analysis started");
});
app.listen(port, () => {
    console.log(`Essentia.js service is running on port :${port}`);
});
client.start();
