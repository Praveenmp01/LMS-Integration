import { expect as expectCDK, SynthUtils, MatchStyle, matchTemplate, haveResource, haveOutput } from '@aws-cdk/assert';
import { Stack, App } from 'aws-cdk-lib';
import { pipeline_forge } from '@bb-fnds/cdk-constructs';
import * as BbPlatformExtensionsLmsIntegrations from '../lib/Extensions-Lms-Integrations';

const app = new App();
const coreStack = new Stack();
const pipelineForgeStack = new pipeline_forge.Stack({
  scope: coreStack,
  projectName: 'dummytest',
  tags: [{ key: 'tag', value: 'value' }],
  stage: 'local',
  environment: { account: '159633141984', region: 'us-east-1' }
});

const LmsIntegrationsStack = new BbPlatformExtensionsLmsIntegrations.ExtLmsIntegrations(pipelineForgeStack);

describe('Platform Extensions LMS Integrations Stack', () => {

  it('Should run a dummy test', async () => {
    expect(true).toBe(true);
  });

});
