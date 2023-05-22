from osbrain import Agent


class OrganizerAgent(Agent):
    def on_init(self):
        self.bind('PUB', alias='initiator')  # to publish to initiator
        self.bind('PUB', alias='responder')  # to publish to responder
        self.bind('PULL', alias='match', handler=self.receive_match)  # to listen for matches

    def start_conversation(self, initiator_id, responder_id):
        # Publish to both agents
        self.send('initiator', (initiator_id, responder_id))
        self.send('responder', (initiator_id, responder_id))

    def receive_match(self, message):
        self.log_info('Match found: ' + message)
        self.shutdown()
