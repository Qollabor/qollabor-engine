package org.cafienne.akka.actor.serialization;

import org.cafienne.cmmn.akka.command.*;
import org.cafienne.cmmn.akka.command.casefile.CreateCaseFileItem;
import org.cafienne.cmmn.akka.command.casefile.DeleteCaseFileItem;
import org.cafienne.cmmn.akka.command.casefile.ReplaceCaseFileItem;
import org.cafienne.cmmn.akka.command.casefile.UpdateCaseFileItem;
import org.cafienne.cmmn.akka.command.casefile.document.AddDocumentInformation;
import org.cafienne.cmmn.akka.command.casefile.document.GetDownloadInformation;
import org.cafienne.cmmn.akka.command.casefile.document.GetUploadInformation;
import org.cafienne.cmmn.akka.command.debug.SwitchDebugMode;
import org.cafienne.cmmn.akka.command.task.CompleteTask;
import org.cafienne.cmmn.akka.command.task.FailTask;
import org.cafienne.cmmn.akka.command.team.PutTeamMember;
import org.cafienne.cmmn.akka.command.team.RemoveTeamMember;
import org.cafienne.cmmn.akka.command.team.SetCaseTeam;
import org.cafienne.humantask.akka.command.*;
import org.cafienne.processtask.akka.command.*;
import org.cafienne.tenant.akka.command.*;
import org.cafienne.tenant.akka.command.platform.CreateTenant;
import org.cafienne.tenant.akka.command.platform.DisableTenant;
import org.cafienne.tenant.akka.command.platform.EnableTenant;
import org.cafienne.timerservice.akka.command.CancelTimer;
import org.cafienne.timerservice.akka.command.SetTimer;
import org.cafienne.timerservice.akka.command.response.TimerServiceResponse;

public class CommandSerializer extends CafienneSerializer {
    static void register() {
        addCaseCommands();
        addProcessActorCommands();
        addTenantCommands();
        addPlatformCommands();
        addTimerServiceCommands();
    }

    private static void addCaseCommands() {
        addManifestWrapper(StartCase.class, StartCase::new);
        addCasePlanCommands();
        addCaseFileCommands();
        addCaseTeamCommands();
        addHumanTaskCommands();
        addManifestWrapper(SwitchDebugMode.class, SwitchDebugMode::new);
    }

    private static void addCasePlanCommands() {
        addManifestWrapper(AddDiscretionaryItem.class, AddDiscretionaryItem::new);
        addManifestWrapper(GetDiscretionaryItems.class, GetDiscretionaryItems::new);
        addManifestWrapper(MakeCaseTransition.class, MakeCaseTransition::new);
        addManifestWrapper(MakePlanItemTransition.class, MakePlanItemTransition::new);
        addManifestWrapper(CompleteTask.class, CompleteTask::new);
        addManifestWrapper(FailTask.class, FailTask::new);
    }

    private static void addCaseFileCommands() {
        addManifestWrapper(CreateCaseFileItem.class, CreateCaseFileItem::new);
        addManifestWrapper(DeleteCaseFileItem.class, DeleteCaseFileItem::new);
        addManifestWrapper(ReplaceCaseFileItem.class, ReplaceCaseFileItem::new);
        addManifestWrapper(UpdateCaseFileItem.class, UpdateCaseFileItem::new);
        addManifestWrapper(AddDocumentInformation.class, AddDocumentInformation::new);
        addManifestWrapper(GetUploadInformation.class, GetUploadInformation::new);
        addManifestWrapper(GetDownloadInformation.class, GetDownloadInformation::new);
    }

    private static void addCaseTeamCommands() {
        addManifestWrapper(PutTeamMember.class, PutTeamMember::new);
        addManifestWrapper(RemoveTeamMember.class, RemoveTeamMember::new);
        addManifestWrapper(SetCaseTeam.class, SetCaseTeam::new);
    }

    private static void addHumanTaskCommands() {
        addManifestWrapper(AssignTask.class, AssignTask::new);
        addManifestWrapper(ClaimTask.class, ClaimTask::new);
        addManifestWrapper(CompleteHumanTask.class, CompleteHumanTask::new);
        addManifestWrapper(DelegateTask.class, DelegateTask::new);
        addManifestWrapper(FillTaskDueDate.class, FillTaskDueDate::new);
        addManifestWrapper(RevokeTask.class, RevokeTask::new);
        addManifestWrapper(SaveTaskOutput.class, SaveTaskOutput::new);
        addManifestWrapper(ValidateTaskOutput.class, ValidateTaskOutput::new);
    }

    private static void addProcessActorCommands() {
        addManifestWrapper(StartProcess.class, StartProcess::new);
        addManifestWrapper(ResumeProcess.class, ResumeProcess::new);
        addManifestWrapper(ReactivateProcess.class, ReactivateProcess::new);
        addManifestWrapper(SuspendProcess.class, SuspendProcess::new);
        addManifestWrapper(TerminateProcess.class, TerminateProcess::new);
    }

    private static void addTenantCommands() {
        addManifestWrapper(UpsertTenantUser.class, UpsertTenantUser::new);
        addManifestWrapper(AddTenantUserRole.class, AddTenantUserRole::new);
        addManifestWrapper(RemoveTenantUserRole.class, RemoveTenantUserRole::new);
        addManifestWrapper(EnableTenantUser.class, EnableTenantUser::new);
        addManifestWrapper(DisableTenantUser.class, DisableTenantUser::new);
        addManifestWrapper(AddTenantOwner.class, AddTenantOwner::new);
        addManifestWrapper(RemoveTenantOwner.class, RemoveTenantOwner::new);
        addManifestWrapper(GetTenantOwners.class, GetTenantOwners::new);
    }

    private static void addPlatformCommands() {
        addManifestWrapper(CreateTenant.class, CreateTenant::new);
        addManifestWrapper(DisableTenant.class, DisableTenant::new);
        addManifestWrapper(EnableTenant.class, EnableTenant::new);
    }

    private static void addTimerServiceCommands() {
        addManifestWrapper(SetTimer.class, SetTimer::new);
        addManifestWrapper(CancelTimer.class, CancelTimer::new);
        addManifestWrapper(TimerServiceResponse.class, TimerServiceResponse::new);
    }
}
