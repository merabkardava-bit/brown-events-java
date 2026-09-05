const BASE_URL = 'http://localhost:8080';

console.log('API base URL:', BASE_URL);

export async function getConferences() {
  console.log('fetching conferences');
  const response = await fetch(`${BASE_URL}/api/conferences`);
  const data = await response.json();
  return data;
}

export async function getConference(id) {
  const response = await fetch(`${BASE_URL}/api/conferences/${id}`);
  return response.json();
}

export async function getConferenceSessions(id) {
  const response = await fetch(`${BASE_URL}/api/conferences/${id}/sessions`);
  const data = await response.json();
  console.log('sessions response:', data);
  return data.data || data;
}

export async function getSession(id, conferenceId) {
  const response = await fetch(`${BASE_URL}/api/sessions/${id}`);
  return response.json();
}

export async function getSpeakers() {
  const response = await fetch(`${BASE_URL}/api/speakers`);
  return response.json();
}

export async function createConference(data) {
  const response = await fetch(`${BASE_URL}/api/conferences`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  });
  return response.json();
}

export async function getConferenceRegistrations(id) {
  const response = await fetch(`${BASE_URL}/api/conferences/${id}/registrations`);
  return response.json();
}

export async function deleteRegistration(conferenceId, registrationId) {
  await fetch(`${BASE_URL}/api/conferences/${conferenceId}/registrations/${registrationId}`, {
    method: 'DELETE'
  });
}

export async function registerAttendee(conferenceId, attendeeData) {
  console.log('registering:', attendeeData);
  const response = await fetch(`${BASE_URL}/api/conferences/${conferenceId}/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(attendeeData)
  });
  return response.json();
}
