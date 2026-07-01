import { request } from './http'

export function getGovernanceOverview() {
  return request('/api/governance/overview')
}

export function getGatewayTraffic() {
  return request('/api/governance/traffic')
}

export function getSystemMonitor() {
  return request('/api/governance/system-monitor')
}
