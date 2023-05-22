from osbrain import run_agent, run_nameserver
from agent_behaviors import InitiatorAgent, ResponderAgent
from organizer import OrganizerAgent
from utils import assign_interests


def main():
    ns = run_nameserver()

    # Create agents and assign interests
    initiators = [run_agent(f'Initiator{i}', base=InitiatorAgent) for i in range(5)]
    responders = [run_agent(f'Responder{i}', base=ResponderAgent) for i in range(5)]
    for agent in initiators + responders:
        assign_interests(agent, 3)

    # Create organizer
    organizer = run_agent('Organizer', base=OrganizerAgent)

    # Connect agents
    for initiator, responder in zip(initiators, responders):
        initiator.connect(organizer.addr('initiator'), handler=initiator.handle_new_responder)
        responder.connect(organizer.addr('responder'), handler=responder.handle_new_initiator)
        organizer.connect(initiator.addr('match'), handler=organizer.receive_match)

    # Start the conversations
    for initiator, responder in zip(initiators, responders):
        organizer.start_conversation(initiator.name, responder.name)

    ns.shutdown()

if __name__ == "__main__":
    main()
