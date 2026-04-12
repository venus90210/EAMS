import { Controller, All, Req, Res, UseInterceptors } from '@nestjs/common';
import { Request, Response } from 'express';
import { ProxyService } from './proxy.service';
import { ValidatedUser } from '../auth/jwt.strategy';

@Controller('api')
export class ProxyController {
  constructor(private proxyService: ProxyService) {}

  @All('*')
  async proxy(@Req() req: Request, @Res() res: Response) {
    const user = req.user as ValidatedUser;

    try {
      const response = await this.proxyService.forward(req, user).toPromise();
      res.status(response.status).json(response.data);
    } catch (error: any) {
      const status = error.response?.status || 500;
      const data = error.response?.data || { error: 'GATEWAY_ERROR', message: error.message };
      res.status(status).json(data);
    }
  }
}
