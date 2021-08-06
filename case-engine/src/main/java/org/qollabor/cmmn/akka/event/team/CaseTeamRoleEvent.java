package org.qollabor.cmmn.akka.event.team;

import com.fasterxml.jackson.core.JsonGenerator;
import org.qollabor.akka.actor.serialization.Fields;
import org.qollabor.cmmn.akka.command.team.MemberKey;
import org.qollabor.cmmn.instance.Case;
import org.qollabor.akka.actor.serialization.json.ValueMap;

import java.io.IOException;

/**
 * Basic event allowing listeners that are interested only in case team member role events to do initial filtering.
 */
public abstract class CaseTeamRoleEvent extends CaseTeamMemberEvent {
    private final String roleName;

    /**
     * Returns true if the role name is blank
     * @return
     */
    public boolean isMemberItself() {
        return roleName.isBlank();
    }

    protected CaseTeamRoleEvent(Case caseInstance, MemberKey member, String roleName) {
        super(caseInstance, member);
        this.roleName = roleName;
    }

    protected CaseTeamRoleEvent(ValueMap json) {
        super(json);
        this.roleName = json.raw(Fields.role);
    }

    public String roleName() {
        return roleName;
    }

    @Override
    public void write(JsonGenerator generator) throws IOException {
        super.write(generator);
        writeField(generator, Fields.role, roleName);
    }
}
