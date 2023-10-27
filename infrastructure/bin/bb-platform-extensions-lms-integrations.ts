#!/usr/bin/env node
import 'source-map-support/register';
import * as cdk from 'aws-cdk-lib';
import { ExtLmsIntegrations } from '../lib/Extensions-Lms-Integrations';
import { pipeline_forge } from "@bb-fnds/cdk-constructs";

const app = new cdk.App();

for (const stack of pipeline_forge.Stack.fromEnv(app)) {
  new ExtLmsIntegrations(stack);
}

