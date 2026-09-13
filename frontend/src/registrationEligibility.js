const CLOSED_STATUSES = ['completed', 'cancelled']

function normalize(status) {
  return status ? String(status).trim().toLowerCase() : ''
}

export function isRegistrationOpen(status) {
  const s = normalize(status)
  return s === '' || !CLOSED_STATUSES.includes(s)
}

export function registrationClosedNotice(status) {
  return normalize(status) === 'cancelled'
    ? 'This conference has been cancelled. Registration is closed.'
    : 'This conference has ended. Registration is closed.'
}
