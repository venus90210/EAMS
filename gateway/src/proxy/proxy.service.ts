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
    const targetUrl = `${this.backendUrl}${request.path}`;

    const headers = { ...request.headers };
    delete headers.authorization;
    delete headers.host;

    headers['X-User-Id'] = user.userId;
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
