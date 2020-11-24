package org.cafienne.akka.actor.serialization;

import org.cafienne.akka.actor.command.response.CommandFailure;
import org.cafienne.akka.actor.command.response.EngineChokedFailure;
import org.cafienne.akka.actor.command.response.SecurityFailure;
import org.cafienne.cmmn.akka.command.response.AddDiscretionaryItemResponse;
import org.cafienne.cmmn.akka.command.response.CaseResponse;
import org.cafienne.cmmn.akka.command.response.CaseStartedResponse;
import org.cafienne.cmmn.akka.command.response.GetDiscretionaryItemsResponse;
import org.cafienne.cmmn.akka.command.response.file.DownloadInformation;
import org.cafienne.cmmn.akka.command.response.file.UploadInformation;
import org.cafienne.humantask.akka.command.response.HumanTaskResponse;
import org.cafienne.humantask.akka.command.response.HumanTaskValidationResponse;
import org.cafienne.processtask.akka.command.response.ProcessResponse;
import org.cafienne.tenant.akka.command.response.TenantOwnersResponse;
import org.cafienne.tenant.akka.command.response.TenantResponse;

public class ResponseSerializer extends CafienneSerializer {
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
        addManifestWrapper(UploadInformation.class, UploadInformation::new);
        addManifestWrapper(DownloadInformation.class, DownloadInformation::new);
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
