import time

from osbrain import Agent, run_agent
from interests import InterestList
from initiator import Initiator
from responder import Responder
import random


class Organizer(Agent):
    def on_init(self):
        # get the organizer's endpoint
        organizer_addr = self.bind('REP', alias='organizer', handler=Organizer.match_found)

        print("Creating: " + str(self.number_of_pairs) + " pairs")
        self.pairs = []
        for i in range(self.number_of_pairs):
            # set the default channel and connection
            channel_alias = 'channel{}'.format(i + 1)
            responder = run_agent('Responder{}'.format(i + 1), base=Responder,
                                  attributes=dict(interests=InterestList.get_random_interests(10)))
            initiator = run_agent('Initiator{}'.format(i + 1), base=Initiator,
                                  attributes=dict(interests=InterestList.get_random_interests(1)))

            # get the responder's endpoint
            responder_addr = responder.bind('REP', alias='ServerSocket', addr=None, handler=Responder.reply)

            # create an address book with endpoints
            initiator.address_book = [responder_addr, organizer_addr]
            for address in initiator.address_book:
                initiator.connect(server=address, alias='ClientSocket')

            self.pairs.append((responder, initiator))


        self.run_interaction()

        # Schedule the periodic task to update channels
        while True:
            time.sleep(3)


    # Handle initiatior's reply
    def match_found(self, message):
        print(message, "got a match!")
        # Shutdown the game
        self.shutdown()

    def run_interaction(self):
        for _, initiator in self.pairs:
            initiator.send_and_receive(random.choice(initiator.interests))

