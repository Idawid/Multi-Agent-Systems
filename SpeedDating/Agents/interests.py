import random


class InterestList:
    interests = ["sports", "traveling", "music", "cooking", "reading",
                 "photography", "gardening", "painting", "dancing", "hiking"]

    @staticmethod
    def get_random_interests(k):
        return random.sample(InterestList.interests, k)