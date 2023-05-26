from osbrain import Agent


class Initiator(Agent):
    def on_init(self):
        print("%s's interests: %s" % (self.name, self.interests))

    def send_and_receive(self, message):
        # Sending message to Responder
        print("Sending message:", message)
        self.send(address='ClientSocket', message=message)
        reply = self.recv(address='ClientSocket')

        # Sending message to Organizer
        print("Received reply:", reply)
        if reply == "YES":
            self.send(address=self.organizer, message=self.name)

    @property
    def interests(self):
        return self._interests

    @interests.setter
    def interests(self, value):
        self._interests = value

    @property
    def sender(self):
        return self._address_book[0]

    @sender.setter
    def sender(self, address):
        self._address_book[0] = address

    @property
    def organizer(self):
        return self._address_book[1]

    @organizer.setter
    def organizer(self, address):
        self._address_book[1] = address

    @property
    def address_book(self):
        return self._address_book

    @address_book.setter
    def address_book(self, addresses):
        self._address_book = [None, None]
        if len(addresses) > 0:
            self._address_book[0] = addresses[0]
        if len(addresses) > 1:
            self._address_book[1] = addresses[1]
