from osbrain import Agent


class InitiatorAgent(Agent):
    def on_init(self):
        self.bind('PUSH', alias='main')
        self.bind('PULL', alias='match', handler=self.send_message)
        self.log_info('Initiator agent initialized')

    def handle_new_responder(self, message):
        self.responder = message[1]
        self.log_info('Received new responder: ' + self.responder)

    def send_message(self, message):
        if 'interest' in message:
            if message['interest'] in self.interests:
                self.send('main', ('Yes', self.name, self.responder))
                self.log_info('Sent Yes to organizer')
            else:
                self.send('main', ('No', self.name, self.responder))
                self.log_info('Sent No to organizer')


class ResponderAgent(Agent):
    def on_init(self):
        self.bind('PULL', alias='main', handler=self.receive_message)
        self.bind('PULL', alias='match', handler=self.send_message)
        self.log_info('Responder agent initialized')

    def handle_new_initiator(self, message):
        self.initiator = message[0]
        self.log_info('Received new initiator: ' + self.initiator)

    def receive_message(self, message):
        self.send('main', {"interest": message})
        self.log_info('Received message and forwarded to initiator')

    def send_message(self, message):
        if message[0] in self.interests:
            self.send('match', ('Yes', self.name, self.initiator))
            self.log_info('Sent Yes to match channel')
        else:
            self.send('match', ('No', self.name, self.initiator))
            self.log_info('Sent No to match channel')
