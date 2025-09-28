export type AuthState = { token?: string; role?: 'ROLE_ADMIN'|'ROLE_USER'; fullName?: string; };
export const auth = {
  get: (): AuthState => JSON.parse(localStorage.getItem('auth')||'{}'),
  set: (s: AuthState) => { localStorage.setItem('auth', JSON.stringify(s)); localStorage.setItem('token', s.token||''); },
  clear: () => { localStorage.removeItem('auth'); localStorage.removeItem('token'); }
};
