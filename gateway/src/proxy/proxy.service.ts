import { Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { HttpService } from '@nestjs/axios';
import { Request } from 'express';
import { Observable } from 'rxjs';
import { AxiosResponse } from 'axios';

export interface ValidatedUser {
  userId: string;
  role: string;
  institutionId?: string;
}

@Injectable()
export class ProxyService {
  private backendUrl: string;

  constructor(
    private httpService: HttpService,
    private configService: ConfigService,
  ) {
    this.backendUrl = this.configService.get<string>('BACKEND_URL') || 'http://localhost:8080';
  }

  forward(request: Request, user: ValidatedUser): Observable<AxiosResponse> {
    // Strip /api prefix: gateway exposes /api/*, backend has controllers at /*
    const backendPath = request.path.replace(/^\/api/, '');
    const targetUrl = `${this.backendUrl}${backendPath}`;

    const headers = { ...request.headers };
    delete headers.authorization;
    delete headers.host;

    // Inject validated user identity for the backend's TenantFilter
    headers['X-User-Id'] = user.userId;
    headers['X-User-Role'] = user.role;
    if (user.institutionId) {
      headers['X-Institution-Id'] = user.institutionId;
    }

    const config = {
      method: request.method.toLowerCase(),
      headers,
      data: request.body,
    };

    return this.httpService.request({
      url: targetUrl,
      ...config,
    });
  }
}
