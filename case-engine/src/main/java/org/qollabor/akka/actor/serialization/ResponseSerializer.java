package org.qollabor.akka.actor.serialization;

import org.qollabor.akka.actor.command.response.CommandFailure;
import org.qollabor.akka.actor.command.response.EngineChokedFailure;
import org.qollabor.akka.actor.command.response.SecurityFailure;
import org.qollabor.cmmn.akka.command.response.AddDiscretionaryItemResponse;
import org.qollabor.cmmn.akka.command.response.CaseResponse;
import org.qollabor.cmmn.akka.command.response.CaseStartedResponse;
import org.qollabor.cmmn.akka.command.response.GetDiscretionaryItemsResponse;
import org.qollabor.humantask.akka.command.response.HumanTaskResponse;
import org.qollabor.humantask.akka.command.response.HumanTaskValidationResponse;
import org.qollabor.processtask.akka.command.response.ProcessResponse;
import org.qollabor.tenant.akka.command.response.TenantOwnersResponse;
import org.qollabor.tenant.akka.command.response.TenantResponse;

public class ResponseSerializer extends QollaborSerializer {
    static void register() {
        addCaseResponses();
        addHumanTaskResponses();
        addProcessResponses();
        addFailureResponses();
        addTenantResponses();
    }

    private static void addCaseResponses() {
        addManifestWrapper(AddDiscretionaryItemResponse.class, AddDiscretionaryItemResponse::new);
        addManifestWrapper(GetDiscretionaryItemsResponse.class, GetDiscretionaryItemsResponse::new);
        addManifestWrapper(CaseStartedResponse.class, CaseStartedResponse::new);
        addManifestWrapper(CaseResponse.class, CaseResponse::new);
    }

    private static void addHumanTaskResponses() {
        addManifestWrapper(HumanTaskResponse.class, HumanTaskResponse::new);
        addManifestWrapper(HumanTaskValidationResponse.class, HumanTaskValidationResponse::new);
    }

    private static void addProcessResponses() {
        addManifestWrapper(ProcessResponse.class, ProcessResponse::new);
    }

    private static void addFailureResponses() {
        addManifestWrapper(CommandFailure.class, CommandFailure::new);
        addManifestWrapper(SecurityFailure.class, SecurityFailure::new);
        addManifestWrapper(EngineChokedFailure.class, EngineChokedFailure::new);
    }

    private static void addTenantResponses() {
        addManifestWrapper(TenantOwnersResponse.class, TenantOwnersResponse::new);
        addManifestWrapper(TenantResponse.class, TenantResponse::new);
    }
}
