from osbrain import Agent


class Responder(Agent):

    def on_init(self):
        print("%s's interests: %s" % (self.name, self.interests))

    def reply(self, message):
        received_interest = str(message)
        if received_interest in self.interests:
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

