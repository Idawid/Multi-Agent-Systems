import random
from osbrain import Agent, run_agent, run_nameserver

interests = ["sports", "traveling", "music", "cooking", "reading",
             "photography", "gardening", "painting", "dancing", "hiking"]


class Responder(Agent):

    def on_init(self):
        print("Responder's interests: ", self.interests)
        self.bind('REP', alias=self.channel, handler=self.reply)

    def reply(self, message):
        received_interest = str(message)
        if received_interest in self._interests:
            response = "YES"
        else:
            response = "NO"
        return response

    @property
    def interests(self):
        return self._interests

    @interests.setter
    def interests(self, value):
        self._interests = value

    @property
    def channel(self):
        return self._channel

    @channel.setter
    def channel(self, value):
        self._channel = value


class Initiator(Agent):

    def on_init(self):
        print("Initiator's interests: ", self._interests)

    def send_and_receive(self, message):
        # Sending message to Responder
        print("Sending message:", message)
        self.send(self.channel, message)
        reply = self.recv(self.channel)

        # Sending message to Organizer
        print("Received reply:", reply)
        if reply == "YES":
            self.send('organizer', "Positive reply received from:" + self.name)

    @property
    def interests(self):
        return self._interests

    @interests.setter
    def interests(self, value):
        self._interests = value

    @property
    def channel(self):
        return self._channel

    @channel.setter
    def channel(self, value):
        self._channel = value


class Organizer(Agent):
    def on_init(self):
        print("Creating: " + str(self.number_of_pairs) + " pairs")
        self.pairs = []
        for i in range(self.number_of_pairs):
            channel_name = 'channel{}'.format(i + 1)
            responder = run_agent('Responder{}'.format(i + 1), base=Responder,
                                  attributes=dict(interests=random.sample(interests, 10), channel=channel_name))
            initiator = run_agent('Initiator{}'.format(i + 1), base=Initiator,
                                  attributes=dict(interests=random.sample(interests, 10), channel=channel_name))

            responder_addr = responder.addr(responder.channel)
            initiator.connect(responder_addr, alias=responder.channel)

            self.pairs.append((responder, initiator))

        self.bind('REP', alias='organizer', handler=self.got_match)
        organizer_addr = self.addr('organizer')
        for _, initiator in self.pairs:
            initiator.connect(organizer_addr, alias='organizer')

    def got_match(self, message):
        print("Got a match! Received message from Initiator:", message)

    def run_interaction(self):
        for _, initiator in self.pairs:
            initiator.send_and_receive(random.choice(initiator.interests))


if __name__ == '__main__':
    ns = run_nameserver()
    K = 2
    organizer = run_agent('Organizer', base=Organizer, attributes=dict(number_of_pairs=K))

    organizer.run_interaction()
    ns.shutdown()