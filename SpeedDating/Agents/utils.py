import random
from interests import INTERESTS


def assign_interests(agent, k):
    agent.interests = random.sample(INTERESTS, k)
