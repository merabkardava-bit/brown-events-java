const BASE_URL = 'http://localhost:8080';

console.log('API base URL:', BASE_URL);

export async function getConferences(page = 0, size = 12, search = '', from = '', to = '', status = '', signal = undefined) {
  console.log('fetching conferences');
  const params = new URLSearchParams({ page, size });
  if (search) params.set('search', search);
  if (from) params.set('from', from);
  if (to) params.set('to', to);
  if (status) params.set('status', status);
  const response = await fetch(`${BASE_URL}/api/conferences?${params.toString()}`, signal ? { signal } : {});
  return response.json();
}

export async function getConference(id) {
  const response = await fetch(`${BASE_URL}/api/conferences/${id}`);
  return response.json();
}

export async function getConferenceSessions(id, page = 0, size = 10) {
  const response = await fetch(`${BASE_URL}/api/conferences/${id}/sessions?page=${page}&size=${size}`);
  const data = await response.json();
  console.log('sessions response:', data);
  // Returns the full paged envelope: { data, page, size, totalElements, totalPages }
  return data;
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
  const res = await response.json();
  return res.data ?? res;
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
