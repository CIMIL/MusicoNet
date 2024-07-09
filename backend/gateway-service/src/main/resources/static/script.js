import {Client} from '@stomp/stompjs';
import {WebSocket} from 'ws';

Object.assign(global, {WebSocket});
const clientWs = new Client({
    brokerURL: 'http://localhost:9095/analysis',
    debug: function (str) {
        console.debug(str.error ? str.error : str);
    },
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
    onConnect: () => {
        const msg = 'Hello, STOMP';
        clientWs.publish({
            destination: '/test', msg
        });
        clientWs.subscribe('/topic/result', (message) => {
            console.log(message.body);
        });

    },
    onStompError: function (frame) {
        // Will be invoked in case of error encountered at Broker
        // Bad login/passcode typically will cause an error
        // Complaint brokers will set `message` header with a brief message. Body may contain details.
        // Compliant brokers will terminate the connection after any error
        console.log('Broker reported error: ' + frame.headers['message']);
        console.log('Additional details: ' + frame.body);
    }
});

function connectToWs() {
    console.log("Connecting to WebSocket")
    clientWs.activate();

}
export {connectToWs};
