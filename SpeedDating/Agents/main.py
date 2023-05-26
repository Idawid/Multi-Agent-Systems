from osbrain import run_agent, run_nameserver
from organizer import Organizer


if __name__ == '__main__':
    ns = run_nameserver()
    pairs = 1
    organizer = run_agent('Organizer', base=Organizer, attributes=dict(number_of_pairs=pairs))

    organizer.run_interaction()
    ns.shutdown()
