package org.qollabor.cmmn.akka.event.team;

import org.qollabor.akka.actor.serialization.Manifest;
import org.qollabor.cmmn.akka.command.team.MemberKey;
import org.qollabor.cmmn.instance.Case;
import org.qollabor.akka.actor.serialization.json.ValueMap;

/**
 * Event caused when a role is removed from a case team member
 */
@Manifest
public class TeamRoleCleared extends CaseTeamRoleEvent {
    public TeamRoleCleared(Case caseInstance, MemberKey member, String roleName) {
        super(caseInstance, member, roleName);
    }

    public TeamRoleCleared(ValueMap json) {
        super(json);
    }

    @Override
    public String getDescription() {
        if (isMemberItself()) {
            // The event that removes the member to the team; in practice this one cannot be invoked
            //  since that can only be done through removing the member explicitly, resulting in TeamMemberRemoved event
            return getClass().getSimpleName() + "[" + getMemberDescription()+" is removed from the case team]";
        } else {
            return getClass().getSimpleName() + "[" + getMemberDescription()+" no longer has role " + roleName() + " in the case team]";
        }
    }

    @Override
    public void updateState(Case actor) {
        actor.getCaseTeam().updateState(this);
    }
}
